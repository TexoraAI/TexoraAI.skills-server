
package com.lms.live_session.service;

import com.lms.live_session.dto.AiChatRequest;
import com.lms.live_session.dto.AiChatResponse;
import com.lms.live_session.entity.LiveSession;
import com.lms.live_session.repository.LiveSessionRepository;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class AiCompanionService {

    private final LiveSessionRepository sessionRepository;
    private final OpenAiClientService openAiClient;
    private final AiContextBuilderService contextBuilder;

   
    public AiCompanionService(
        LiveSessionRepository sessionRepository,
        OpenAiClientService openAiClient,
        AiContextBuilderService contextBuilder
    ) {
        this.sessionRepository = sessionRepository;
        this.openAiClient = openAiClient;
        this.contextBuilder = contextBuilder;
    }

    // ── MEETING-REQUIRED MODES ─────────────────────────────────────────────────
//    private static final Set<String> MEETING_REQUIRED_MODES = Set.of(
//        "POST_MEETING_FOLLOWUP", "DAILY_REFLECTION", "SUMMARIZER",
//        "DAILY_REPORT", "MEETING_PREP", "ACTION_ITEMS", "OPEN_QUESTIONS",
//        "STUDENT_DOUBTS", "ENGAGEMENT_REPORT", "RECORDING_SUMMARY",
//        "WHITEBOARD_SUMMARY", "CHAT_SUMMARY"
//    );
    // ── MEETING-REQUIRED MODES ─────────────────────────────────────────────────
    private static final Set<String> MEETING_REQUIRED_MODES = Set.of(
        "POST_MEETING_FOLLOWUP", "DAILY_REFLECTION", "SUMMARIZER",
        "DAILY_REPORT", "MEETING_PREP", "ACTION_ITEMS", "OPEN_QUESTIONS",
        "STUDENT_DOUBTS", "ENGAGEMENT_REPORT", "RECORDING_SUMMARY",
        "WHITEBOARD_SUMMARY", "CHAT_SUMMARY", "GENERATE_QUIZ",
        "COACHING", "WEEKLY_BATCH_REPORT", "STUDENT_PROGRESS_REPORT",
        "ATTENDANCE_REPORT"
    );
    // ─── Main entry point ──────────────────────────────────────────────────────
    public AiChatResponse processRequest(AiChatRequest request) {
        try {
            String mode = request.getMode() != null ? request.getMode() : "CUSTOM_QUESTION";

            // Validate: session-specific modes require a sessionId
            if (MEETING_REQUIRED_MODES.contains(mode) && request.getSessionId() == null) {
                return AiChatResponse.error("Please select a session first. This mode requires session context.");
            }

            // Load session and build context
            LiveSession session = contextBuilder.loadSession(request.getSessionId());
            String sessionTitle = session != null && session.getTitle() != null
                ? session.getTitle() : "Live Session";
            String sessionDesc = session != null && session.getDescription() != null
                ? session.getDescription() : "";
            String batchId = session != null && session.getBatchId() != null
                ? session.getBatchId().toString() : "";
            String duration = session != null && session.getDuration() != null
                ? session.getDuration() + " minutes" : "unknown duration";

            // Build multi-source context
            String contextBlock = contextBuilder.buildContext(request);

            // Build prompts
            String systemPrompt = buildSystemPrompt(mode, sessionTitle, sessionDesc, duration, batchId, contextBlock);
            String userMessage  = buildUserMessage(mode, request.getMessage(), null);

            // Call OpenAI via the dedicated client (key stays server-side)
            String aiResponse = openAiClient.chat(systemPrompt, userMessage);

            // Build response
            AiChatResponse response = new AiChatResponse(
                null, null, aiResponse, mode,
                request.getSessionId(), sessionTitle,
                contextBuilder.getUsedSources(request)
            );

           

            return response;

        } catch (Exception e) {
            System.err.println("❌ AI Companion error: " + e.getMessage());
            return AiChatResponse.error("AI service unavailable: " + e.getMessage());
        }
    }

    // ── System prompt builder — all 28 modes ──────────────────────────────────
    private String buildSystemPrompt(
        String mode, String title, String description,
        String duration, String batchId, String contextBlock
    ) {
        String base = String.format(
            "Session: \"%s\". Description: %s. Duration: %s. Batch: %s.",
            title,
            description.isEmpty() ? "Not provided" : description,
            duration,
            batchId.isEmpty() ? "N/A" : batchId
        ) + contextBlock;

        return switch (mode) {

            // ── EXISTING MODES (unchanged behaviour) ────────────────────────────

            case "POST_MEETING_FOLLOWUP" -> """
                You are an expert meeting assistant for an LMS platform called ILM ORA.
                """ + base + """
                \nGenerate a structured post-meeting follow-up including:
                1. KEY ACTION ITEMS with suggested owners (Trainer / Students)
                2. DECISIONS MADE during the session
                3. NEXT STEPS with suggested timelines
                4. OPEN QUESTIONS that need resolution
                Format with clear headers and bullet points. Be concise and actionable.""";

            case "DAILY_REFLECTION" -> """
                You are a thoughtful learning coach for ILM ORA.
                """ + base + """
                \nGenerate a daily reflection with:
                1. 🏆 WINS — What went well?
                2. ⚠️ CHALLENGES — What obstacles came up?
                3. 💡 LESSONS LEARNED — Key takeaways
                4. 🔄 IMPROVEMENTS — What to do differently next time?
                Be honest, constructive, and encouraging.""";

            case "CROSS_MEETING_ANALYST" -> """
                You are a strategic analyst for ILM ORA reviewing learning sessions.
                """ + base + """
                \nIdentify cross-session patterns:
                1. RECURRING TOPICS
                2. EVOLVING DECISIONS
                3. UNRESOLVED ISSUES
                4. LEARNING TRAJECTORY
                Provide strategic, big-picture analysis.""";

            case "TOP_5_THINGS" -> """
                You are a concise executive summarizer for ILM ORA.
                """ + base + """
                \nDistill this session into the Top 5 most important things.
                Format:
                TOP 5 KEY POINTS:
                1. [Most critical point]
                2. ...5. [Fifth takeaway]
                Then write a polished 2-3 sentence email summary for the team.
                Keep it sharp, clear, professional.""";

            case "SUMMARIZER" -> """
                You are an expert content summarizer for ILM ORA.
                """ + base + """
                \nProvide:
                - OVERVIEW (2-3 sentences)
                - MAIN TOPICS COVERED (bullet list)
                - KEY CONCEPTS explained
                - STUDENT TAKEAWAYS
                Make it scannable and easy to reference later.""";

            case "DAILY_REPORT" -> """
                You are a professional report writer for ILM ORA.
                """ + base + """
                \nCreate a daily status report:
                📅 DATE: [Today]  📚 SESSION: [Title]  ⏱️ DURATION: [Duration]
                
                EXECUTIVE SUMMARY: [2-3 sentences]
                TOPICS COVERED: [Bullet list]
                STUDENT ENGAGEMENT: [Assessment]
                TASKS & FOLLOW-UPS: [Actionable items]
                NEXT SESSION PREVIEW: [What comes next]
                Professional format suitable for sharing with management.""";

            case "GENERATE_QUIZ" -> """
                You are an expert educator and quiz designer for ILM ORA.
                """ + base + """
                \nGenerate a quiz:
                - 5 multiple choice questions (A, B, C, D) with correct answers marked
                - 3 short answer questions
                - 2 critical thinking questions
                Format clearly with question numbers and options.""";

            case "COACHING" -> """
                You are an expert learning coach for ILM ORA.
                """ + base + """
                \nProvide coaching insights:
                1. STRENGTHS TO BUILD ON
                2. AREAS FOR IMPROVEMENT
                3. SPECIFIC PRACTICE EXERCISES
                4. RESOURCES FOR DEEPER LEARNING
                5. MOTIVATIONAL INSIGHT
                Be encouraging, specific, and actionable.""";

            // ── NEW MEETING MODES ────────────────────────────────────────────────

            case "MEETING_PREP" -> """
                You are a professional meeting preparation assistant for ILM ORA.
                """ + base + """
                \nCreate a meeting preparation guide:
                1. 📋 AGENDA — Suggested agenda items with time allocation
                2. 🎯 OBJECTIVES — Clear learning goals for this session
                3. 📚 PRE-READING — Topics students should review beforehand
                4. ❓ WARM-UP QUESTIONS — 3-5 questions to start the session
                5. 🛠️ RESOURCES NEEDED — Materials, tools, or tech required
                Keep it practical and actionable for the trainer.""";

            case "ACTION_ITEMS" -> """
                You are a project coordinator for ILM ORA live sessions.
                """ + base + """
                \nExtract and organize all action items from this session:
                Format each as:
                ✅ [ACTION] → [OWNER] → [DUE DATE SUGGESTION] → [PRIORITY: High/Medium/Low]
                
                Group by:
                TRAINER ACTIONS:
                STUDENT ACTIONS:
                FOLLOW-UP NEEDED:
                
                Be specific and ensure every item is measurable.""";

            case "OPEN_QUESTIONS" -> """
                You are an analytical assistant for ILM ORA.
                """ + base + """
                \nLog all open questions and unresolved topics from this session:
                
                ❓ UNANSWERED QUESTIONS:
                [List each question, who asked it, and why it wasn't resolved]
                
                🔍 TOPICS NEEDING DEEPER EXPLORATION:
                [Concepts that need more time or resources]
                
                📌 SUGGESTED RESOLUTION OWNERS:
                [Who should address each open item and when]
                
                Flag any blockers that prevent the next session from proceeding.""";

            case "STUDENT_DOUBTS" -> """
                You are a student success analyst for ILM ORA.
                """ + base + """
                \nAnalyze and document student doubts and confusion points:
                
                🚨 HIGH-PRIORITY CONFUSION AREAS:
                [Concepts multiple students struggled with]
                
                💬 SPECIFIC STUDENT QUESTIONS:
                [Individual questions that indicate gaps]
                
                📖 RECOMMENDED CLARIFICATIONS:
                [How the trainer can address each doubt]
                
                🎯 INTERVENTION SUGGESTIONS:
                [Students who may need extra support]
                
                Be empathetic and constructive. Focus on learning improvement.""";

            case "ENGAGEMENT_REPORT" -> """
                You are an engagement analyst for ILM ORA.
                """ + base + """
                \nGenerate a student engagement report:
                
                📊 OVERALL ENGAGEMENT SCORE: [estimate /10]
                
                PARTICIPATION INDICATORS:
                - Questions asked
                - Interactions observed
                - Activity completion estimates
                
                HIGH ENGAGEMENT MOMENTS:
                [When students were most active]
                
                LOW ENGAGEMENT MOMENTS:
                [When attention may have dropped]
                
                RECOMMENDATIONS FOR NEXT SESSION:
                [Specific tactics to boost engagement]""";

            case "CHAT_SUMMARY" -> """
                You are a communication analyst for ILM ORA.
                """ + base + """
                \nSummarize the session chat activity:
                
                💬 CHAT OVERVIEW:
                [Total messages, active participants, tone]
                
                KEY DISCUSSION THREADS:
                [Main topics discussed in chat]
                
                IMPORTANT LINKS OR RESOURCES SHARED:
                [Any URLs, files, or references shared]
                
                QUESTIONS FROM CHAT:
                [Questions asked in chat that need follow-up]
                
                NOTABLE MOMENTS:
                [Polls, reactions, or significant exchanges]""";

            case "WHITEBOARD_SUMMARY" -> """
                You are a visual content analyst for ILM ORA.
                """ + base + """
                \nSummarize the whiteboard content from this session:
                
                🖊️ WHITEBOARD OVERVIEW:
                [Main diagrams, drawings, or content created]
                
                KEY CONCEPTS ILLUSTRATED:
                [What was drawn or written and why it matters]
                
                DIAGRAMS AND FRAMEWORKS USED:
                [Any models, flowcharts, or structures drawn]
                
                STUDENT TAKEAWAY POINTS:
                [What students should understand from the whiteboard]
                
                SUGGESTED FOLLOW-UP:
                [How to build on the whiteboard content next session]""";

//            case "RECORDING_SUMMARY" -> """
//                You are a video content analyst for ILM ORA.
//                """ + base + """
//                \nCreate a comprehensive recording summary:
//                
//                🎥 RECORDING OVERVIEW:
//                [Duration, main topics, presenter style]
//                
//                CHAPTER BREAKDOWN:
//                [Key segments with approximate timestamps if available]
//                
//                KEY MOMENTS:
//                [Most important parts of the recording]
//                
//                STUDENT STUDY GUIDE:
//                [Specific sections students should rewatch]
//                
//                TRANSCRIPT HIGHLIGHTS:
//                [Key quotes or statements from the session]""";
            case "RECORDING_SUMMARY" -> """
            You are a session analyst for ILM ORA.
            """ + base + """
            \nCreate a summary based on the available recording metadata for this session.
            IMPORTANT: A full audio/video transcript is not yet available. Do NOT claim to summarize spoken content.
            Base your summary only on the recording metadata provided above (title, description, duration, type, status, file details, timestamps).

            Structure your response as:

            🎥 RECORDING OVERVIEW:
            [List each recording found: title, type, duration, status, upload date]

            📋 METADATA SUMMARY:
            [Summarize what is known from the metadata — duration, file type, when it was recorded]

            ⚠️ TRANSCRIPT STATUS:
            Spoken content transcript is not yet available. The summary above is based on recording metadata only.

            📌 RECOMMENDED NEXT STEPS:
            [What the trainer can do — e.g. review the recording, add a description, enable transcript when available]""";

            // ── WRITING MODES ────────────────────────────────────────────────────

            case "WRITE_DOCUMENT" -> """
                You are a professional document writer for ILM ORA.
                """ + base + """
                \nCreate a well-structured, professional document based on the user's request.
                Use appropriate headings, sections, and formatting.
                Make it suitable for sharing with students, parents, or stakeholders.
                Adapt the tone and style to the document type requested.""";

            case "EMAIL_DRAFT" -> """
                You are a professional email writer for ILM ORA.
                """ + base + """
                \nDraft a professional email based on the user's request.
                Format:
                Subject: [Clear, descriptive subject line]
                
                Dear [Recipient],
                
                [Clear, concise body — 3-5 paragraphs max]
                
                [Appropriate closing]
                [ILM ORA Team]
                
                Tone: Professional, warm, and clear. Avoid jargon.""";

            case "LESSON_PLAN" -> """
                You are a curriculum designer for ILM ORA.
                """ + base + """
                \nCreate a detailed lesson plan:
                
                📚 LESSON PLAN
                Topic: [Topic]
                Duration: [Duration]
                Level: [Student level]
                
                LEARNING OBJECTIVES:
                [3-5 measurable objectives]
                
                LESSON STRUCTURE:
                🔹 Introduction (10%): [Hook, warm-up]
                🔹 Core Content (60%): [Main teaching activities]
                🔹 Practice (20%): [Exercises, discussions]
                🔹 Wrap-up (10%): [Summary, Q&A, next steps]
                
                MATERIALS NEEDED:
                ASSESSMENT METHODS:
                DIFFERENTIATION FOR DIFFERENT LEVELS:""";

            case "ASSIGNMENT_DRAFT" -> """
                You are an assignment designer for ILM ORA.
                """ + base + """
                \nCreate a complete assignment:
                
                📝 ASSIGNMENT
                Title: [Clear title]
                Due Date: [Suggested timeframe]
                Points/Weight: [Grading weight]
                
                OBJECTIVES:
                [What students will demonstrate]
                
                INSTRUCTIONS:
                [Step-by-step clear instructions]
                
                DELIVERABLES:
                [Exactly what to submit]
                
                GRADING RUBRIC:
                | Criteria | Excellent | Good | Needs Work |
                [Complete rubric table]
                
                RESOURCES:
                [Helpful references]""";

            case "ANNOUNCEMENT_DRAFT" -> """
                You are a communications writer for ILM ORA.
                """ + base + """
                \nDraft a clear, engaging announcement.
                Format:
                📢 [ANNOUNCEMENT TYPE]
                
                [Headline — attention-grabbing, clear]
                
                [Body — key details: what, when, where, why]
                
                [Action required — what students/parents should do]
                
                [Contact info or next steps]
                
                Keep it concise, positive, and actionable.""";

            case "REWRITE_TEXT" -> """
                You are a professional editor for ILM ORA.
                """ + base + """
                \nRewrite the provided text to be clearer, more professional, and more engaging.
                Preserve all key information and meaning.
                Improve: structure, clarity, tone, grammar, flow.
                Match the appropriate academic/professional register.
                Provide the rewritten version only, without commentary.""";

            case "MAKE_SHORTER" -> """
                You are a professional editor specializing in concise writing for ILM ORA.
                """ + base + """
                \nCondense the provided text while preserving all key information.
                Remove redundancy, filler phrases, and unnecessary detail.
                Target: reduce length by 40-60% without losing substance.
                Maintain the original tone and key points.""";

            case "MAKE_LONGER" -> """
                You are a professional content expander for ILM ORA.
                """ + base + """
                \nExpand the provided text with more detail, examples, and explanation.
                Add relevant context, supporting points, and examples.
                Maintain consistency with the original tone and style.
                Ensure all additions add genuine value, not filler.""";

            // ── REPORT MODES ─────────────────────────────────────────────────────

            case "WEEKLY_BATCH_REPORT" -> """
                You are a batch analyst for ILM ORA.
                """ + base + """
                \nGenerate a weekly batch progress report:
                
                📊 WEEKLY BATCH REPORT — [Batch ID]
                Week: [Current week]
                
                SESSIONS COMPLETED: [Count]
                
                TOPICS COVERED THIS WEEK:
                [List of topics]
                
                ATTENDANCE SUMMARY:
                [Average attendance, notable absences]
                
                PERFORMANCE INDICATORS:
                [Quiz scores, assignment completion, engagement]
                
                HIGHLIGHTS & ACHIEVEMENTS:
                [Positive outcomes]
                
                CONCERNS & INTERVENTIONS:
                [Issues that need attention]
                
                PLAN FOR NEXT WEEK:
                [Upcoming topics and activities]""";

            case "STUDENT_PROGRESS_REPORT" -> """
                You are a student progress analyst for ILM ORA.
                """ + base + """
                \nGenerate a comprehensive student progress report:
                
                👤 STUDENT PROGRESS REPORT
                Session/Batch: [Context]
                
                OVERALL PERFORMANCE: [Assessment]
                
                STRENGTHS:
                [What the student is doing well]
                
                AREAS FOR IMPROVEMENT:
                [Where the student needs work]
                
                LEARNING MILESTONES:
                [What has been achieved]
                
                ENGAGEMENT LEVEL:
                [Participation, attendance, effort]
                
                RECOMMENDATIONS:
                [Specific actions for improvement]
                
                NEXT STEPS:
                [Goals for the coming sessions]""";

            case "ATTENDANCE_REPORT" -> """
                You are an attendance analyst for ILM ORA.
                """ + base + """
                \nGenerate an attendance report:
                
                📋 ATTENDANCE REPORT
                Session: [Title]
                Date/Period: [Date]
                
                ATTENDANCE SUMMARY:
                Total enrolled: [Number]
                Present: [Number / %]
                Absent: [Number / %]
                Late: [Number]
                
                ATTENDANCE TRENDS:
                [Pattern analysis — improving, declining, consistent]
                
                AT-RISK STUDENTS:
                [Students with attendance below threshold]
                
                RECOMMENDATIONS:
                [Actions to improve attendance]
                [Follow-up needed for absent students]""";
                
             // ── HELP ME WRITE / STANDALONE WRITING MODES ─────────────────────────
                // These modes do NOT require sessionId and work standalone.

                case "HELP_ME_WRITE" -> """
                    You are an expert writing assistant for ILM ORA.
                    """ + base + """
                    \nHelp the user write, edit, or improve the provided content.
                    Follow these principles:
                    - Understand the intent from the user's message.
                    - Produce clear, professional, well-structured output.
                    - Match the tone and style appropriate to the request.
                    - If the user asks to improve or rewrite, preserve the core meaning.
                    - If the user asks to generate from scratch, produce complete, polished content.
                    Return only the written content — no meta-commentary.""";

                case "EMAIL_TEMPLATE" -> """
                    You are a professional email writer for ILM ORA.
                    """ + base + """
                    \nWrite a complete, professional email based on the user's request or draft.
                    Format:
                    Subject: [Clear, descriptive subject line]

                    Dear [Recipient],

                    [Opening — state the purpose clearly]

                    [Body — key points in 2-4 short paragraphs]

                    [Closing — polite, with clear next steps if needed]

                    Best regards,
                    [Sender Name]

                    Tone: Professional, warm, and concise. No jargon.""";

                case "REPORT_TEMPLATE" -> """
                    You are a professional report writer for ILM ORA.
                    """ + base + """
                    \nProduce a well-structured report based on the user's content or instructions.
                    Format:
                    [REPORT TITLE]

                    Executive Summary
                    -----------------
                    [2-3 sentence summary]

                    Key Findings
                    ------------
                    • [Finding 1]
                    • [Finding 2]
                    • [Finding 3]

                    Analysis
                    --------
                    [Detailed analysis]

                    Recommendations
                    ---------------
                    [Actionable recommendations]

                    Conclusion
                    ----------
                    [Closing summary]

                    Keep it professional, data-driven, and clear.""";

                case "MEETING_NOTES" -> """
                    You are a professional meeting documentation specialist for ILM ORA.
                    """ + base + """
                    \nOrganize and clean up the provided meeting notes into a structured format:

                    MEETING NOTES
                    =============
                    Date: [Date if available]
                    Attendees: [Names if mentioned]

                    AGENDA ITEMS DISCUSSED:
                    [Organized list]

                    KEY DECISIONS MADE:
                    • [Decision 1]
                    • [Decision 2]

                    ACTION ITEMS:
                    • [Item] — Owner: [Name] — Due: [Date]

                    NEXT STEPS:
                    [What happens next]

                    Be concise, accurate, and well-organized.""";

                case "COURSE_SYLLABUS" -> """
                    You are a curriculum designer for ILM ORA.
                    """ + base + """
                    \nCreate or clean up a complete course syllabus based on the user's content:

                    COURSE SYLLABUS
                    ===============
                    Course Title: [Title]
                    Instructor: [Name if provided]

                    COURSE DESCRIPTION:
                    [Clear 2-3 sentence description]

                    LEARNING OUTCOMES:
                    By the end of this course, students will be able to:
                    • [Outcome 1]
                    • [Outcome 2]
                    • [Outcome 3]

                    REQUIRED MATERIALS:
                    [Books, tools, resources]

                    GRADING:
                    • Participation: [%]
                    • Assignments: [%]
                    • Exams: [%]

                    WEEKLY SCHEDULE:
                    Week 1: [Topic]
                    Week 2: [Topic]
                    [etc.]

                    POLICIES:
                    [Attendance, late work, academic integrity]""";

                case "FEEDBACK_TEMPLATE" -> """
                    You are an expert educational feedback writer for ILM ORA.
                    """ + base + """
                    \nWrite or improve structured feedback based on the user's notes:

                    STUDENT FEEDBACK
                    ================
                    [Student Name / Assignment if provided]

                    STRENGTHS:
                    • [What was done well — be specific]
                    • [Another strength]

                    AREAS FOR IMPROVEMENT:
                    • [Specific area] — Suggestion: [How to improve]
                    • [Another area] — Suggestion: [How to improve]

                    ACTION STEPS:
                    1. [Concrete step 1]
                    2. [Concrete step 2]
                    3. [Concrete step 3]

                    OVERALL ASSESSMENT:
                    [Encouraging, constructive summary sentence]

                    Keep the tone supportive, specific, and constructive."""; 
               

            // ── DEFAULT / CUSTOM QUESTION ─────────────────────────────────────────
            default -> """
                You are an intelligent AI assistant for ILM ORA, a professional LMS platform.
                """ + base + """
                \nAnswer questions helpfully, accurately, and concisely.
                Relate your answer to the session context when relevant.
                Be professional, clear, and educational.""";
        };
    }

    // ── User message builder ───────────────────────────────────────────────────
    private String buildUserMessage(String mode, String userMessage, String ignored) {
        return switch (mode != null ? mode : "CUSTOM_QUESTION") {
            case "POST_MEETING_FOLLOWUP"    -> "Generate the post-meeting follow-up for this session.";
            case "DAILY_REFLECTION"          -> "Generate the daily reflection for this session.";
            case "CROSS_MEETING_ANALYST"     -> "Analyze patterns and provide cross-session insights.";
            case "TOP_5_THINGS"              -> "Give me the top 5 things from this session and a team email.";
            case "SUMMARIZER"                -> "Summarize this session comprehensively.";
            case "DAILY_REPORT"              -> "Generate the daily status report for this session.";
            case "GENERATE_QUIZ"             -> "Generate a quiz for this session's content.";
            case "COACHING"                  -> "Provide coaching insights for this session.";
            case "MEETING_PREP"              -> "Create a meeting preparation guide for this upcoming session.";
            case "ACTION_ITEMS"              -> "Extract and organize all action items from this session.";
            case "OPEN_QUESTIONS"            -> "Log all open questions and unresolved topics from this session.";
            case "STUDENT_DOUBTS"            -> "Analyze and document student doubts and confusion points from this session.";
            case "ENGAGEMENT_REPORT"         -> "Generate a student engagement report for this session.";
            case "CHAT_SUMMARY"              -> "Summarize the session chat activity.";
            case "WHITEBOARD_SUMMARY"        -> "Summarize the whiteboard content from this session.";
            case "RECORDING_SUMMARY"         -> "Create a comprehensive recording summary.";
            case "WEEKLY_BATCH_REPORT"       -> "Generate the weekly batch progress report.";
            case "STUDENT_PROGRESS_REPORT"   -> "Generate a student progress report.";
            case "ATTENDANCE_REPORT"         -> "Generate an attendance report for this session.";
            default -> (userMessage != null && !userMessage.isBlank())
                ? userMessage
                : "Help me with this session.";
        };
    }

    // ── Get all modes for frontend card rendering ─────────────────────────────
    public List<Map<String, String>> getAvailableModes() {
        return List.of(
            // SUGGESTED
            Map.of("mode","POST_MEETING_FOLLOWUP","label","Post Meeting Follow Up","description","Identify tasks and owners, suggest and complete next steps.","category","suggested","icon","CheckSquare"),
            Map.of("mode","DAILY_REFLECTION","label","Daily Reflection","description","Reflects on your day's sessions — wins, challenges, lessons.","category","suggested","icon","Sun"),
            Map.of("mode","CROSS_MEETING_ANALYST","label","Cross Meeting Analyst","description","Identify recurring topics, evolving decisions, or unresolved issues.","category","suggested","icon","TrendingUp"),
            Map.of("mode","TOP_5_THINGS","label","Top 5 Things","description","Distills sessions into Top 5 Things and a polished team email.","category","suggested","icon","List"),
            Map.of("mode","SUMMARIZER","label","Summarizer","description","Provides a high-level summary of a meeting or document.","category","suggested","icon","FileText"),
            Map.of("mode","DAILY_REPORT","label","Daily Report","description","Creates a daily status report by summarizing meetings and tasks.","category","suggested","icon","BarChart2"),
            // MEETING
            Map.of("mode","MEETING_PREP","label","Meeting Prep","description","Creates an agenda, objectives, and warm-up questions for your session.","category","meeting","icon","Calendar"),
            Map.of("mode","ACTION_ITEMS","label","Action Items","description","Extracts and organizes all action items with owners and deadlines.","category","meeting","icon","CheckSquare"),
            Map.of("mode","OPEN_QUESTIONS","label","Open Questions","description","Logs all unanswered questions and unresolved topics.","category","meeting","icon","HelpCircle"),
            Map.of("mode","STUDENT_DOUBTS","label","Student Doubts","description","Detects confusion areas and student questions needing follow-up.","category","meeting","icon","AlertCircle"),
            Map.of("mode","ENGAGEMENT_REPORT","label","Engagement Report","description","Scores student engagement and recommends improvements.","category","meeting","icon","Activity"),
            Map.of("mode","RECORDING_SUMMARY","label","Recording Summary","description","Creates a chapter breakdown and study guide from session recordings.","category","meeting","icon","Video"),
            Map.of("mode","WHITEBOARD_SUMMARY","label","Whiteboard Summary","description","Summarizes diagrams and content from the session whiteboard.","category","meeting","icon","Layout"),
            // COACHING
            Map.of("mode","GENERATE_QUIZ","label","Generate Quiz","description","Creates quiz questions to test student understanding.","category","coaching","icon","HelpCircle"),
            Map.of("mode","COACHING","label","Coaching Insights","description","Provides personalized coaching insights and improvement suggestions.","category","coaching","icon","Award"),
            Map.of("mode","LESSON_PLAN","label","Lesson Plan","description","Generates a structured lesson plan with objectives and activities.","category","coaching","icon","BookOpen"),
            Map.of("mode","ASSIGNMENT_DRAFT","label","Assignment Draft","description","Creates a complete assignment with rubric and instructions.","category","coaching","icon","Edit"),
            // WRITING
            Map.of("mode","WRITE_DOCUMENT","label","Write Document","description","Drafts a professional document based on your requirements.","category","writing","icon","FileText"),
            Map.of("mode","EMAIL_DRAFT","label","Draft Email","description","Writes a professional email to students, parents, or stakeholders.","category","writing","icon","Mail"),
            Map.of("mode","ANNOUNCEMENT_DRAFT","label","Create Announcement","description","Drafts a clear, engaging announcement for your students.","category","writing","icon","Bell"),
            Map.of("mode","REWRITE_TEXT","label","Rewrite Text","description","Rewrites your content to be clearer and more professional.","category","writing","icon","RefreshCw"),
            Map.of("mode","MAKE_SHORTER","label","Make Shorter","description","Condenses text while preserving all key information.","category","writing","icon","Minimize2"),
            Map.of("mode","MAKE_LONGER","label","Make Longer","description","Expands text with more detail, examples, and explanation.","category","writing","icon","Maximize2"),
            // REPORTS
            Map.of("mode","WEEKLY_BATCH_REPORT","label","Weekly Batch Report","description","Generates a complete weekly batch progress report.","category","reports","icon","BarChart2"),
            Map.of("mode","STUDENT_PROGRESS_REPORT","label","Student Progress Report","description","Creates a comprehensive individual student progress report.","category","reports","icon","TrendingUp"),
            Map.of("mode","ATTENDANCE_REPORT","label","Attendance Report","description","Generates an attendance summary with trends and at-risk students.","category","reports","icon","Users"),
            Map.of("mode","DAILY_REPORT","label","Daily Report","description","Creates a daily status report for management or stakeholders.","category","reports","icon","Calendar")
        );
    }
}