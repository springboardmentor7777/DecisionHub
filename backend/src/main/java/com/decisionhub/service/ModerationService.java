package com.decisionhub.service;

import com.decisionhub.dto.ApiDtos.ModerationCommentResponse;
import com.decisionhub.entity.AppUser;
import com.decisionhub.entity.BoardComment;
import com.decisionhub.entity.BoardVisibility;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.UserNotification;
import com.decisionhub.repository.BoardCommentRepository;
import com.decisionhub.repository.DecisionBoardRepository;
import com.decisionhub.repository.UserNotificationRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Content moderation. Moderators and administrators can only see and remove content on
 * PUBLIC boards; private boards stay private to their owners.
 */
@Service
public class ModerationService {
    private final BoardCommentRepository comments;
    private final DecisionBoardRepository boards;
    private final BoardService boardService;
    private final AuditService audit;
    private final UserNotificationRepository notifications;

    public ModerationService(BoardCommentRepository comments, DecisionBoardRepository boards,
                             BoardService boardService, AuditService audit,
                             UserNotificationRepository notifications) {
        this.comments = comments;
        this.boards = boards;
        this.boardService = boardService;
        this.audit = audit;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public List<ModerationCommentResponse> recentComments() {
        return comments.findTop50ByBoard_VisibilityOrderByCreatedAtDesc(BoardVisibility.PUBLIC).stream()
                .map(comment -> new ModerationCommentResponse(comment.getId(), comment.getBoard().getId(),
                        comment.getBoard().getTitle(),
                        comment.getParent() == null ? null : comment.getParent().getId(),
                        comment.getAuthor().getId(), comment.getAuthor().getDisplayName(),
                        comment.getContent(), comment.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void deleteComment(Long id, String moderatorEmail) {
        BoardComment comment = comments.findByIdAndBoard_Visibility(id, BoardVisibility.PUBLIC)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        Long boardId = comment.getBoard().getId();
        String boardTitle = comment.getBoard().getTitle();
        AppUser author = comment.getAuthor();
        BoardComment parent = comment.getParent();

        if (parent != null) {
            parent.getReplies().remove(comment); // orphan removal deletes the reply
        } else {
            comments.delete(comment); // cascades to its replies
        }

        notifications.save(new UserNotification(author,
                "A moderator removed your comment on \"" + shorten(boardTitle, 120) + "\"",
                "/boards/" + boardId));
        audit.record(moderatorEmail, "COMMENT_REMOVED", "COMMENT", id,
                "Board " + boardId + ", author " + author.getEmail());
    }

    @Transactional
    public void deleteBoard(Long id, String moderatorEmail) {
        DecisionBoard board = boards.findWithOptionsById(id)
                .filter(found -> found.getVisibility() == BoardVisibility.PUBLIC)
                .orElseThrow(() -> new ResourceNotFoundException("Decision board not found"));
        AppUser owner = board.getOwner();
        String title = board.getTitle();

        boardService.removeBoard(board);

        notifications.save(new UserNotification(owner,
                "A moderator removed your decision \"" + shorten(title, 150) + "\"", null));
        audit.record(moderatorEmail, "BOARD_REMOVED", "BOARD", id,
                "\"" + shorten(title, 150) + "\" by " + owner.getEmail());
    }

    private static String shorten(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}
