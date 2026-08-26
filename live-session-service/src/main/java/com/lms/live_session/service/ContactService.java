package com.lms.live_session.service;

import com.lms.live_session.dto.ContactRequestDTO;
import com.lms.live_session.dto.ContactResponseDTO;
import com.lms.live_session.entity.Contact;
import com.lms.live_session.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContactService {

    private final ContactRepository contactRepository;

    @Autowired
    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public ContactResponseDTO createContact(ContactRequestDTO dto, String creatorId, String creatorName) {
        if (!StringUtils.hasText(dto.getFirstName())) {
            throw new IllegalArgumentException("First name is required");
        }
        if (!StringUtils.hasText(dto.getEmail())) {
            throw new IllegalArgumentException("Email is required");
        }

        if (contactRepository.existsByCreatorIdAndEmail(creatorId, dto.getEmail())) {
            throw new IllegalArgumentException("A contact with this email already exists");
        }

        Contact contact = new Contact();
        contact.setFirstName(dto.getFirstName());
        contact.setLastName(dto.getLastName());
        contact.setEmail(dto.getEmail());
        contact.setRole(dto.getRole());
        contact.setPhone(dto.getPhone());
        contact.setOrganization(dto.getOrganization());
        contact.setNotes(dto.getNotes());
        contact.setCreatorId(creatorId);
        contact.setCreatorName(creatorName);

        Contact saved = contactRepository.save(contact);
        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ContactResponseDTO> getMyContacts(String creatorId) {
        List<Contact> contacts = contactRepository.findByCreatorIdOrderByCreatedAtDesc(creatorId);

        return contacts.stream()
                .sorted(Comparator
                        .comparing((Contact c) -> c.getLastName() == null ? "" : c.getLastName(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(c -> c.getFirstName() == null ? "" : c.getFirstName(), String.CASE_INSENSITIVE_ORDER))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContactResponseDTO getContactById(Long contactId, String creatorId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

        verifyOwnership(contact, creatorId);

        return mapToDTO(contact);
    }

    public ContactResponseDTO updateContact(Long contactId, ContactRequestDTO dto, String creatorId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

        verifyOwnership(contact, creatorId);

        contact.setFirstName(dto.getFirstName());
        contact.setLastName(dto.getLastName());
        contact.setEmail(dto.getEmail());
        contact.setPhone(dto.getPhone());
        contact.setRole(dto.getRole());
        contact.setOrganization(dto.getOrganization());
        contact.setNotes(dto.getNotes());

        Contact saved = contactRepository.save(contact);
        return mapToDTO(saved);
    }

    public void deleteContact(Long contactId, String creatorId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

        verifyOwnership(contact, creatorId);

        contactRepository.delete(contact);
    }

    @Transactional(readOnly = true)
    public List<ContactResponseDTO> searchContacts(String creatorId, String query) {
        Map<Long, Contact> results = new LinkedHashMap<>();

        contactRepository.findByCreatorIdAndFirstNameContainingIgnoreCase(creatorId, query)
                .forEach(c -> results.put(c.getId(), c));

        contactRepository.findByCreatorIdAndLastNameContainingIgnoreCase(creatorId, query)
                .forEach(c -> results.put(c.getId(), c));

        contactRepository.findByCreatorIdAndEmailContainingIgnoreCase(creatorId, query)
                .forEach(c -> results.put(c.getId(), c));

        return results.values().stream()
                .sorted(Comparator
                        .comparing((Contact c) -> c.getLastName() == null ? "" : c.getLastName(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(c -> c.getFirstName() == null ? "" : c.getFirstName(), String.CASE_INSENSITIVE_ORDER))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getEmailSuggestions(String creatorId, String query) {
        Map<Long, Contact> results = new LinkedHashMap<>();

        contactRepository.findByCreatorIdAndEmailContainingIgnoreCase(creatorId, query)
                .forEach(c -> results.put(c.getId(), c));

        contactRepository.findByCreatorIdAndFirstNameContainingIgnoreCase(creatorId, query)
                .forEach(c -> results.put(c.getId(), c));

        contactRepository.findByCreatorIdAndLastNameContainingIgnoreCase(creatorId, query)
                .forEach(c -> results.put(c.getId(), c));

        return results.values().stream()
                .map(Contact::getEmail)
                .distinct()
                .collect(Collectors.toList());
    }

    private void verifyOwnership(Contact contact, String creatorId) {
        if (!contact.getCreatorId().equals(creatorId)) {
            throw new IllegalArgumentException("You do not have permission to access this contact");
        }
    }

    private ContactResponseDTO mapToDTO(Contact contact) {
        ContactResponseDTO dto = new ContactResponseDTO(
                contact.getId(),
                contact.getFirstName(),
                contact.getLastName(),
                contact.getEmail(),
                contact.getPhone(),
                contact.getOrganization(),
                contact.getNotes(),
                contact.getCreatorId(),
                contact.getCreatorName(),
                contact.getCreatedAt(),
                contact.getUpdatedAt()
        );
        dto.setRole(contact.getRole());
        return dto;
    }
}