package com.decisionhub.service;

import com.decisionhub.entity.BoardVisibility;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.User;
import com.decisionhub.exception.ForbiddenException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.repository.ComparisonFactorRepository;
import com.decisionhub.repository.DecisionBoardRepository;
import com.decisionhub.repository.DecisionOptionRepository;
import com.decisionhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DecisionBoardAccessTest {
    private DecisionBoardRepository boardRepository;
    private DecisionBoardService service;

    @BeforeEach
    void setUp() {
        boardRepository = mock(DecisionBoardRepository.class);
        service = new DecisionBoardService(
                boardRepository,
                mock(DecisionOptionRepository.class),
                mock(ComparisonFactorRepository.class),
                mock(UserRepository.class));
    }

    @Test
    void hidesPrivateBoardsFromOtherUsers() {
        DecisionBoard board = boardOwnedBy("owner@example.com", BoardVisibility.PRIVATE);
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));

        assertThrows(ResourceNotFoundException.class,
                () -> service.accessibleBoard("other@example.com", 1L));
    }

    @Test
    void rejectsUpdatesByNonOwners() {
        DecisionBoard board = boardOwnedBy("owner@example.com", BoardVisibility.PUBLIC);
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));

        assertThrows(ForbiddenException.class,
                () -> service.ownedBoard("other@example.com", 1L));
    }

    private static DecisionBoard boardOwnedBy(String email, BoardVisibility visibility) {
        User owner = new User();
        owner.setEmail(email);
        DecisionBoard board = new DecisionBoard();
        board.setId(1L);
        board.setCreatedBy(owner);
        board.setVisibility(visibility);
        return board;
    }
}
