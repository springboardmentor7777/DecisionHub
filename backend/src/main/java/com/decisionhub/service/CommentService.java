
package com.decisionhub.service;

import com.decisionhub.entity.*;
import com.decisionhub.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final DecisionRepository decisionRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository,
                          DecisionRepository decisionRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.decisionRepository = decisionRepository;
        this.userRepository = userRepository;
    }

    public Comment addComment(Long decisionId, Long parentId, String text) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new RuntimeException("Decision not found"));

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setDecision(decision);
        comment.setCommentText(text);
        comment.setCreatedAt(LocalDateTime.now());

        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));

            if (!parent.getDecision().getDecisionId().equals(decisionId)) {
                throw new IllegalArgumentException("Parent comment belongs to another decision");
            }

            comment.setParentComment(parent);
        }

        return commentRepository.save(comment);
    }

    public List<Comment> getComments(Long decisionId) {
        return commentRepository.findByDecision_DecisionIdOrderByCreatedAtAsc(decisionId);
    }
}
