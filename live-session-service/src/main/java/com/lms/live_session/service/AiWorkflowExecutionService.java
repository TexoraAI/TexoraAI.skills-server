package com.lms.live_session.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lms.live_session.dto.AiChatRequest;
import com.lms.live_session.dto.AiChatResponse;
import com.lms.live_session.entity.AiTranscriptSegment;
import com.lms.live_session.entity.AiTranscriptSession;
import com.lms.live_session.entity.AiWorkflow;
import com.lms.live_session.entity.AiWorkflowRun;
import com.lms.live_session.entity.AiWorkflowTask;
import com.lms.live_session.entity.LiveSession;
import com.lms.live_session.event.SessionNotificationEvent;
import com.lms.live_session.kafka.NotificationProducer;
import com.lms.live_session.repository.AiTranscriptSegmentRepository;
import com.lms.live_session.repository.AiTranscriptSessionRepository;
import com.lms.live_session.repository.AiWorkflowRepository;
import com.lms.live_session.repository.AiWorkflowRunRepository;
import com.lms.live_session.repository.AiWorkflowTaskRepository;
import com.lms.live_session.repository.LiveSessionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Executes an AiWorkflow's nodesJson graph.
 *
 * Phase 4 status:
 *   - trigger nodes  → logged as "fired" (manual trigger via /run; automatic
 *                       triggers for t1/t2/t3/t4 are hooked in from
 *                       LiveSessionService / AiTranscriptService via
 *                       fireTrigger() — see TRIGGER_* constants below. t5
 *                       (attendance threshold) is NOT hooked here — it needs
 *                       a @Scheduled poller, out of scope for this pass.)
 *   - ai nodes       → routed to AiCompanionService.processRequest() using a
 *                       node-id → AI mode mapping, saveToHistory=false
 *   - control nodes  → c1 (if condition): best-effort single-comparison
 *                       evaluator against known session fields (duration,
 *                       batchId only). Cannot evaluate anything else
 *                       (attendance, AND/OR) — logs SKIPPED with reason
 *                       rather than guessing. FALSE means "skip all
 *                       remaining nodes in this run" (nodesJson is a flat
 *                       list with no real branch structure — documented
 *                       limitation, not a bug).
 *                       c2 (delay): synchronous Thread.sleep capped at
 *                       MAX_SYNCHRONOUS_DELAY_SECONDS. Longer requests are
 *                       SKIPPED rather than blocking the HTTP thread
 *                       indefinitely or being silently PAUSED forever (no
 *                       scheduler exists yet to resume a paused run).
 *                       c3 (stop): halts all remaining nodes immediately;
 *                       run still completes as COMPLETED (intentional stop
 *                       is not a failure).
 *   - action nodes   → ac1 (save to notes): finds/creates an
 *                       AiTranscriptSession for the run's sessionId and
 *                       appends a segment with the most recent preceding
 *                       `ai` node's output.
 *                       ac2 (send email to trainer) / ac3 (notify students):
 *                       publish via NotificationProducer, consumed by
 *                       LiveSessionEventConsumer.
 *                       ac4 (create follow-up task): no Task/assignment
 *                       entity existed elsewhere in the codebase, so this
 *                       creates a minimal, feature-scoped AiWorkflowTask row.
 *
 * Each node's outcome is appended to a JSON array stored on
 * AiWorkflowRun.resultJson.
 */
@Service
public class AiWorkflowExecutionService {

    private final AiWorkflowRepository workflowRepository;
    private final AiWorkflowRunRepository workflowRunRepository;
    private final AiCompanionService aiCompanionService;
    private final NotificationProducer notificationProducer;
    private final LiveSessionRepository sessionRepository;
    private final AiTranscriptSessionRepository transcriptSessionRepository;
    private final AiTranscriptSegmentRepository transcriptSegmentRepository;
    private final AiWorkflowTaskRepository workflowTaskRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // eventType values published on the EXISTING "session-notifications" topic.
    private static final String EVENT_TYPE_WORKFLOW_TRAINER_EMAIL = "WORKFLOW_TRAINER_EMAIL";
    private static final String EVENT_TYPE_WORKFLOW_NOTIFY_STUDENTS = "WORKFLOW_NOTIFY_STUDENTS";

    // Default source set used for workflow-triggered AI calls.
    private static final List<String> DEFAULT_SOURCES =
            Arrays.asList("MEETINGS", "CHAT", "WHITEBOARD", "RECORDINGS");

    // Node id (from NODE_LIBRARY in AiWorkflowCreate.jsx) → AiCompanionService mode
    private static final Map<String, String> AI_NODE_MODE_MAP = Map.of(
            "a1", "SUMMARIZER",
            "a2", "ACTION_ITEMS",
            "a3", "ENGAGEMENT_REPORT",
            "a4", "GENERATE_QUIZ",
            "a5", "CHAT_SUMMARY"
    );

    // ── ASSUMPTION — verify against the actual AiWorkflow.status values your
    // create/edit flow writes. ──────────────────────────────────────────────
    private static final String ACTIVE_STATUS = "ACTIVE";

    // ── ASSUMPTION — verify these exactly match what your frontend
    // NODE_LIBRARY trigger labels save into AiWorkflow.triggerType. ────────
    public static final String TRIGGER_SESSION_SCHEDULED = "Live session scheduled";
    public static final String TRIGGER_SESSION_STARTED = "Live session started";
    public static final String TRIGGER_SESSION_ENDED = "Live session ended";
    public static final String TRIGGER_TRANSCRIPT_CREATED = "Transcript created";
    // t5 — NOT hooked anywhere yet, needs a @Scheduled poller. Out of scope.
    public static final String TRIGGER_ATTENDANCE_BELOW_THRESHOLD = "Attendance below threshold";

    // c2 delay cap — see class javadoc for the tradeoff explanation.
    private static final long MAX_SYNCHRONOUS_DELAY_SECONDS = 60;

    // c1 best-effort condition evaluator: single comparison only, against a
    // known session field. No AND/OR, no attendance (not available here).
    private static final Pattern SIMPLE_CONDITION_PATTERN = Pattern.compile(
            "^\\s*(session_duration|duration|batch_id|batchId)\\s*(<=|>=|==|!=|<|>)\\s*(-?\\d+(?:\\.\\d+)?)\\s*$",
            Pattern.CASE_INSENSITIVE
    );

    // ac4 default follow-up window when the node config doesn't specify
    // dueInDays.
    private static final int DEFAULT_TASK_DUE_IN_DAYS = 3;

    public AiWorkflowExecutionService(
            AiWorkflowRepository workflowRepository,
            AiWorkflowRunRepository workflowRunRepository,
            AiCompanionService aiCompanionService,
            NotificationProducer notificationProducer,
            LiveSessionRepository sessionRepository,
            AiTranscriptSessionRepository transcriptSessionRepository,
            AiTranscriptSegmentRepository transcriptSegmentRepository,
            AiWorkflowTaskRepository workflowTaskRepository) {
        this.workflowRepository = workflowRepository;
        this.workflowRunRepository = workflowRunRepository;
        this.aiCompanionService = aiCompanionService;
        this.notificationProducer = notificationProducer;
        this.sessionRepository = sessionRepository;
        this.transcriptSessionRepository = transcriptSessionRepository;
        this.transcriptSegmentRepository = transcriptSegmentRepository;
        this.workflowTaskRepository = workflowTaskRepository;
    }

    // ----------------------------------------------------------------
    // Extract trainer email from JWT — same pattern as AiWorkflowService
    // ----------------------------------------------------------------
    private String getCurrentTrainerEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("Unauthorized: no authenticated user found");
        }
        return auth.getName();
    }

    /**
     * Manual entry point — used by POST /{id}/run. Requires the current
     * authenticated user to own the workflow.
     */
    public AiWorkflowRun runWorkflow(Long workflowId, Long sessionId) {
        String trainerEmail = getCurrentTrainerEmail();

        AiWorkflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow not found: " + workflowId));

        if (!trainerEmail.equalsIgnoreCase(workflow.getTrainerEmail())) {
            throw new RuntimeException("Access denied: workflow does not belong to current trainer");
        }

        return executeWorkflowRun(workflow, sessionId, trainerEmail);
    }

    /**
     * Automatic entry point — used by fireTrigger() when a session lifecycle
     * event matches an ACTIVE workflow's triggerType. There is no
     * interactive user here, so we run as the workflow's own owner rather
     * than relying on SecurityContextHolder (which may not reflect the
     * workflow owner in an automated context).
     */
    private AiWorkflowRun runWorkflowAutomatically(AiWorkflow workflow, Long sessionId) {
        return executeWorkflowRun(workflow, sessionId, workflow.getTrainerEmail());
    }

    /**
     * Looks up ACTIVE workflows owned by trainerEmail whose triggerType
     * matches triggerLabel, and runs each of them against sessionId.
     * Never throws — failures here must not break the caller's primary
     * flow (session create/start/end, transcript start, etc.).
     */
    public void fireTrigger(String triggerLabel, Long sessionId, String trainerEmail) {
        if (triggerLabel == null || trainerEmail == null) {
            return;
        }
        List<AiWorkflow> matches;
        try {
            matches = workflowRepository.findByStatusAndTriggerTypeAndTrainerEmail(
                    ACTIVE_STATUS, triggerLabel, trainerEmail);
        } catch (Exception e) {
            System.err.println("[AiWorkflowExecutionService] Trigger lookup failed for '"
                    + triggerLabel + "': " + e.getMessage());
            return;
        }
        for (AiWorkflow workflow : matches) {
            try {
                runWorkflowAutomatically(workflow, sessionId);
            } catch (Exception e) {
                System.err.println("[AiWorkflowExecutionService] Auto-run failed for workflow "
                        + workflow.getId() + " (trigger '" + triggerLabel + "'): " + e.getMessage());
            }
        }
    }

    // ----------------------------------------------------------------
    // Shared run execution — creates the AiWorkflowRun, iterates nodes,
    // honors control-flow signals (STOP / SKIP_REMAINING) from control
    // nodes, tracks the most recent `ai` node output for ac1, and updates
    // the parent workflow's lastRunStatus/lastRunAt.
    // ----------------------------------------------------------------
    private AiWorkflowRun executeWorkflowRun(AiWorkflow workflow, Long sessionId, String trainerEmail) {
        AiWorkflowRun run = new AiWorkflowRun();
        run.setWorkflowId(workflow.getId());
        run.setSessionId(sessionId);
        run.setTriggeredBy(trainerEmail);
        run.setStatus("RUNNING");
        run = workflowRunRepository.save(run);

        ArrayNode resultsArray = objectMapper.createArrayNode();
        boolean anyNodeFailed = false;

        try {
            List<Map<String, Object>> nodes = parseNodes(workflow.getNodesJson());

            if (nodes.isEmpty()) {
                ObjectNode note = objectMapper.createObjectNode();
                note.put("info", "Workflow has no nodes to execute.");
                resultsArray.add(note);
            }

            String lastAiOutput = null;
            boolean haltRemaining = false;
            String haltReason = null;

            for (Map<String, Object> node : nodes) {
                if (haltRemaining) {
                    resultsArray.add(buildSkippedResult(node, haltReason));
                    continue;
                }

                ObjectNode nodeResult = executeNode(node, sessionId, trainerEmail, lastAiOutput, workflow.getId(), run.getId());
                resultsArray.add(nodeResult);

                String nodeType = str(node.get("type"));
                String nodeStatus = nodeResult.has("status") ? nodeResult.get("status").asText() : null;

                if ("FAILED".equals(nodeStatus)) {
                    anyNodeFailed = true;
                }
                if ("ai".equals(nodeType) && "SUCCESS".equals(nodeStatus) && nodeResult.has("output")) {
                    lastAiOutput = nodeResult.get("output").asText();
                }
                if (nodeResult.has("controlSignal")) {
                    String signal = nodeResult.get("controlSignal").asText();
                    if ("STOP".equals(signal)) {
                        haltRemaining = true;
                        haltReason = "Workflow stopped by an upstream 'Stop workflow' control node.";
                    } else if ("SKIP_REMAINING".equals(signal)) {
                        haltRemaining = true;
                        haltReason = "Skipped: an upstream 'If condition' node evaluated FALSE. "
                                + "nodesJson is a flat ordered list with no real branch structure, so all "
                                + "remaining nodes in this run are skipped.";
                    }
                }
            }

            run.setStatus(anyNodeFailed ? "FAILED" : "COMPLETED");
            run.setResultJson(objectMapper.writeValueAsString(resultsArray));
            run.setCompletedAt(LocalDateTime.now());
            run = workflowRunRepository.save(run);

        } catch (Exception e) {
            run.setStatus("FAILED");
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", "Workflow execution failed: " + e.getMessage());
            try {
                resultsArray.add(errorNode);
                run.setResultJson(objectMapper.writeValueAsString(resultsArray));
            } catch (Exception ignored) {
                run.setResultJson("[{\"error\":\"Workflow execution failed\"}]");
            }
            run.setCompletedAt(LocalDateTime.now());
            run = workflowRunRepository.save(run);
        }

        workflow.setLastRunStatus(run.getStatus());
        workflow.setLastRunAt(run.getCompletedAt() != null ? run.getCompletedAt() : LocalDateTime.now());
        workflowRepository.save(workflow);

        return run;
    }

    // ----------------------------------------------------------------
    // Node execution
    // ----------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private ObjectNode executeNode(Map<String, Object> node, Long sessionId, String trainerEmail,
                                    String lastAiOutput, Long workflowId, Long workflowRunId) {
        ObjectNode result = objectMapper.createObjectNode();
        String instanceId = str(node.get("instanceId"));
        String nodeId = str(node.get("id"));
        String type = str(node.get("type"));
        String label = str(node.get("label"));

        result.put("instanceId", instanceId);
        result.put("nodeId", nodeId);
        result.put("type", type);
        result.put("label", label);

        try {
            switch (type == null ? "" : type) {

                case "trigger" -> {
                    result.put("status", "SKIPPED");
                    result.put("output", "Trigger acknowledged (manual run) — automatic trigger evaluation is handled outside /run via fireTrigger().");
                }

                case "ai" -> {
                    if (!AI_NODE_MODE_MAP.containsKey(nodeId)) {
                        result.put("status", "SKIPPED");
                        result.put("output", "No AI mode mapping found for node id '" + nodeId + "'.");
                        break;
                    }
                    if (sessionId == null) {
                        result.put("status", "FAILED");
                        result.put("output", "This AI step requires a session, but no sessionId was provided to the run.");
                        break;
                    }

                    String mode = AI_NODE_MODE_MAP.get(nodeId);

                    AiChatRequest chatRequest = new AiChatRequest();
                    chatRequest.setSessionId(sessionId);
                    chatRequest.setMode(mode);
                    chatRequest.setMessage(null);
                    chatRequest.setSources(DEFAULT_SOURCES);
                    chatRequest.setSaveToHistory(false);
                    chatRequest.setConversationId(null);

                    AiChatResponse aiResponse = aiCompanionService.processRequest(chatRequest, trainerEmail);

                    if (aiResponse.isSuccess()) {
                        result.put("status", "SUCCESS");
                        result.put("mode", mode);
                        result.put("output", aiResponse.getResponse());
                    } else {
                        result.put("status", "FAILED");
                        result.put("mode", mode);
                        result.put("output", aiResponse.getError());
                    }
                }

                case "action" -> {
                    switch (nodeId == null ? "" : nodeId) {

                        case "ac1" -> executeSaveToNotes(sessionId, trainerEmail, lastAiOutput, result);

                        case "ac2" -> executeSendTrainerEmail(node, sessionId, trainerEmail, result);

                        case "ac3" -> executeNotifyStudents(node, sessionId, trainerEmail, result);

                        case "ac4" -> executeCreateTask(node, sessionId, trainerEmail, workflowId, workflowRunId, result);

                        default -> {
                            result.put("status", "SKIPPED");
                            result.put("output", "Action '" + label + "' is not yet implemented in the execution engine.");
                        }
                    }
                }

                case "control" -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> config = (Map<String, Object>) node.getOrDefault("config", Map.of());
                    switch (nodeId == null ? "" : nodeId) {
                        case "c1" -> executeIfCondition(config, sessionId, result);
                        case "c2" -> executeDelay(config, result);
                        case "c3" -> executeStop(config, result);
                        default -> {
                            result.put("status", "SKIPPED");
                            result.put("output", "Control node '" + label + "' (id=" + nodeId + ") is not recognized.");
                        }
                    }
                }

                default -> {
                    result.put("status", "SKIPPED");
                    result.put("output", "Unknown node type: " + type);
                }
            }
        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("output", "Node execution error: " + e.getMessage());
        }

        return result;
    }

    // ----------------------------------------------------------------
    // c1 — If condition (best-effort, single comparison only)
    // ----------------------------------------------------------------
    private void executeIfCondition(Map<String, Object> config, Long sessionId, ObjectNode result) {
        String condition = str(config.get("condition"));
        if (condition == null || condition.isBlank()) {
            result.put("status", "SKIPPED");
            result.put("output", "Condition node has no condition configured; skipping.");
            return;
        }

        Boolean evaluated = evaluateSimpleCondition(condition, sessionId);

        if (evaluated == null) {
            result.put("status", "SKIPPED");
            result.put("output", "Condition '" + condition + "' could not be evaluated — only simple "
                    + "single comparisons against known session fields (duration, batchId) are supported "
                    + "(no AND/OR, no attendance data available here). Skipping rather than guessing.");
            return;
        }

        if (evaluated) {
            result.put("status", "SUCCESS");
            result.put("output", "Condition '" + condition + "' evaluated TRUE — continuing.");
        } else {
            result.put("status", "SUCCESS");
            result.put("output", "Condition '" + condition + "' evaluated FALSE. nodesJson is a flat "
                    + "ordered list with no real branch structure, so all remaining nodes in this run "
                    + "are skipped (documented limitation, not a bug).");
            result.put("controlSignal", "SKIP_REMAINING");
        }
    }

    private Boolean evaluateSimpleCondition(String condition, Long sessionId) {
        if (condition == null || sessionId == null) {
            return null;
        }
        String trimmed = condition.trim();
        String upper = trimmed.toUpperCase();
        if (upper.contains(" AND ") || upper.contains(" OR ")) {
            return null;
        }

        Matcher m = SIMPLE_CONDITION_PATTERN.matcher(trimmed);
        if (!m.matches()) {
            return null;
        }

        String field = m.group(1).toLowerCase();
        String op = m.group(2);
        double rhs = Double.parseDouble(m.group(3));

        LiveSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null) {
            return null;
        }

        Double lhs;
        switch (field) {
            case "session_duration", "duration" ->
                    lhs = session.getDuration() != null ? Double.valueOf(session.getDuration()) : null;
            case "batch_id", "batchid" ->
                    lhs = session.getBatchId() != null ? Double.valueOf(session.getBatchId()) : null;
            default -> lhs = null;
        }
        if (lhs == null) {
            return null;
        }

        return switch (op) {
            case "<" -> lhs < rhs;
            case ">" -> lhs > rhs;
            case "<=" -> lhs <= rhs;
            case ">=" -> lhs >= rhs;
            case "==" -> lhs.doubleValue() == rhs;
            case "!=" -> lhs.doubleValue() != rhs;
            default -> null;
        };
    }

    // ----------------------------------------------------------------
    // c2 — Delay (synchronous, capped)
    // ----------------------------------------------------------------
    private void executeDelay(Map<String, Object> config, ObjectNode result) {
        Integer delayMinutes = toInteger(config.get("delayMinutes"));

        if (delayMinutes == null || delayMinutes <= 0) {
            result.put("status", "SKIPPED");
            result.put("output", "Delay node has no valid delayMinutes configured; skipping.");
            return;
        }

        long requestedSeconds = delayMinutes * 60L;

        if (requestedSeconds > MAX_SYNCHRONOUS_DELAY_SECONDS) {
            result.put("status", "SKIPPED");
            result.put("output", "Requested delay of " + delayMinutes + " minute(s) exceeds the "
                    + MAX_SYNCHRONOUS_DELAY_SECONDS + "-second synchronous cap. Execution is synchronous "
                    + "within POST /{id}/run, so longer delays need a scheduler (not implemented yet) — "
                    + "skipping the wait and continuing rather than blocking the request or pausing forever.");
            return;
        }

        try {
            Thread.sleep(requestedSeconds * 1000L);
            result.put("status", "SUCCESS");
            result.put("output", "Delayed " + delayMinutes + " minute(s) synchronously before continuing.");
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            result.put("status", "FAILED");
            result.put("output", "Delay was interrupted before completing.");
        }
    }

    private Integer toInteger(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(val.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    // ----------------------------------------------------------------
    // c3 — Stop workflow
    // ----------------------------------------------------------------
    private void executeStop(Map<String, Object> config, ObjectNode result) {
        String stopReason = str(config.get("stopReason"));
        result.put("status", "SUCCESS");
        result.put("output", "Workflow stopped intentionally."
                + (stopReason != null && !stopReason.isBlank() ? " Reason: " + stopReason : ""));
        result.put("controlSignal", "STOP");
    }

    // ----------------------------------------------------------------
    // ac1 — Save to In-Person Notes
    // ----------------------------------------------------------------
    private void executeSaveToNotes(Long sessionId, String trainerEmail, String lastAiOutput, ObjectNode result) {
        try {
            if (sessionId == null) {
                result.put("status", "FAILED");
                result.put("output", "Cannot save to notes: no sessionId provided to this run.");
                return;
            }

            AiTranscriptSession ts = transcriptSessionRepository
                    .findFirstByLiveSessionIdOrderByStartedAtDesc(sessionId)
                    .orElseGet(() -> {
                        AiTranscriptSession newTs = new AiTranscriptSession();
                        newTs.setLiveSessionId(sessionId);
                        newTs.setTrainerEmail(trainerEmail);
                        newTs.setTitle("Workflow Notes");
                        newTs.setStatus(AiTranscriptSession.TranscriptStatus.RECORDING);
                        return transcriptSessionRepository.save(newTs);
                    });

            String noteText = (lastAiOutput != null && !lastAiOutput.isBlank())
                    ? lastAiOutput
                    : "Workflow note (no prior AI-generated output was available in this run).";

            AiTranscriptSegment segment = new AiTranscriptSegment();
            segment.setTranscriptSessionId(ts.getId());
            segment.setText(noteText);
            segment.setSpeakerName("Workflow");
            segment.setStartedAtSecond(0);
            transcriptSegmentRepository.save(segment);

            result.put("status", "SUCCESS");
            result.put("output", "Saved to In-Person Notes (transcript session #" + ts.getId() + ").");
        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("output", "Failed to save to notes: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // ac2 — Send email to trainer
    // ----------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void executeSendTrainerEmail(Map<String, Object> node, Long sessionId, String trainerEmail, ObjectNode result) {
        try {
            LiveSession session = sessionId != null ? sessionRepository.findById(sessionId).orElse(null) : null;

            Map<String, Object> config = (Map<String, Object>) node.getOrDefault("config", Map.of());
            String configuredRecipient = str(config.get("recipient"));
            String customMessage = str(config.get("message"));

            String recipient = (configuredRecipient != null && !configuredRecipient.isBlank())
                    ? configuredRecipient
                    : trainerEmail;

            String bodyText = (customMessage != null && !customMessage.isBlank())
                    ? customMessage
                    : "Your workflow completed for this session.";

            SessionNotificationEvent event = new SessionNotificationEvent(
                    sessionId,
                    trainerEmail,
                    session != null ? session.getBatchId() : null,
                    session != null ? session.getTitle() : "Live Session",
                    session != null && session.getScheduledDate() != null ? session.getScheduledDate().toString() : null,
                    session != null && session.getScheduledTime() != null ? session.getScheduledTime().toString() : null,
                    session != null ? session.getDuration() : null,
                    EVENT_TYPE_WORKFLOW_TRAINER_EMAIL,
                    recipient,
                    null,
                    "TRAINER",
                    null
            );
            event.setWorkflowOutputText(bodyText);
            notificationProducer.sendWorkflowTrainerEmail(event);

            result.put("status", "SUCCESS");
            result.put("output", "Email sent to trainer (" + recipient + ") via the notification service.");
        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("output", "Failed to publish trainer email event: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // ac3 — Notify students
    // ----------------------------------------------------------------
    private void executeNotifyStudents(Map<String, Object> node, Long sessionId, String trainerEmail, ObjectNode result) {
        try {
            if (sessionId == null) {
                result.put("status", "FAILED");
                result.put("output", "Cannot notify students: no sessionId provided to this run.");
                return;
            }
            LiveSession session = sessionRepository.findById(sessionId).orElse(null);

            @SuppressWarnings("unchecked")
            Map<String, Object> config = (Map<String, Object>) node.getOrDefault("config", Map.of());
            String customMessage = str(config.get("message"));
            String bodyText = (customMessage != null && !customMessage.isBlank())
                    ? customMessage
                    : "A workflow-triggered update is available for this session.";

            SessionNotificationEvent event = new SessionNotificationEvent(
                    sessionId,
                    trainerEmail,
                    session != null ? session.getBatchId() : null,
                    session != null ? session.getTitle() : "Live Session",
                    session != null && session.getScheduledDate() != null ? session.getScheduledDate().toString() : null,
                    session != null && session.getScheduledTime() != null ? session.getScheduledTime().toString() : null,
                    session != null ? session.getDuration() : null,
                    EVENT_TYPE_WORKFLOW_NOTIFY_STUDENTS,
                    null,
                    null,
                    "STUDENT",
                    null
            );
            event.setWorkflowOutputText(bodyText);

            if (session == null || session.getBatchId() == null) {
                result.put("status", "FAILED");
                result.put("output", "Cannot notify students: session " + sessionId + " has no batchId, so the notification consumer cannot resolve recipients.");
                return;
            }

            notificationProducer.sendWorkflowNotifyStudents(event);

            result.put("status", "SUCCESS");
            result.put("output", "Students in batch " + session.getBatchId() + " notified for session " + sessionId + ".");
        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("output", "Failed to publish student notification event: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // ac4 — Create follow-up task
    // ----------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void executeCreateTask(Map<String, Object> node, Long sessionId, String trainerEmail,
                                    Long workflowId, Long workflowRunId, ObjectNode result) {
        try {
            Map<String, Object> config = (Map<String, Object>) node.getOrDefault("config", Map.of());

            String configuredTitle = str(config.get("title"));
            String description = str(config.get("description"));
            Integer dueInDays = toInteger(config.get("dueInDays"));

            LiveSession session = sessionId != null ? sessionRepository.findById(sessionId).orElse(null) : null;

            String title = (configuredTitle != null && !configuredTitle.isBlank())
                    ? configuredTitle
                    : "Follow-up: " + (session != null && session.getTitle() != null ? session.getTitle() : "Live Session");

            AiWorkflowTask task = new AiWorkflowTask();
            task.setWorkflowId(workflowId);
            task.setWorkflowRunId(workflowRunId);
            task.setSessionId(sessionId);
            task.setTrainerEmail(trainerEmail);
            task.setTitle(title);
            task.setDescription(description);
            task.setDueDate(LocalDate.now().plusDays(dueInDays != null && dueInDays > 0 ? dueInDays : DEFAULT_TASK_DUE_IN_DAYS));
            task.setStatus("OPEN");

            AiWorkflowTask saved = workflowTaskRepository.save(task);

            result.put("status", "SUCCESS");
            result.put("output", "Follow-up task #" + saved.getId() + " (\"" + title + "\") created, due " + saved.getDueDate() + ".");
        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("output", "Failed to create follow-up task: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private ObjectNode buildSkippedResult(Map<String, Object> node, String reason) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("instanceId", str(node.get("instanceId")));
        result.put("nodeId", str(node.get("id")));
        result.put("type", str(node.get("type")));
        result.put("label", str(node.get("label")));
        result.put("status", "SKIPPED");
        result.put("output", reason);
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseNodes(String nodesJson) {
        if (nodesJson == null || nodesJson.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(nodesJson);
            if (!root.isArray()) {
                return List.of();
            }
            return objectMapper.convertValue(root, List.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse workflow nodesJson: " + e.getMessage());
        }
    }

    private String str(Object val) {
        return val == null ? null : val.toString();
    }
}