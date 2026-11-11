package com.decisionhub.service;

import com.decisionhub.dto.ApiDtos.DashboardResponse;
import com.decisionhub.repository.BoardVoteRepository;
import com.decisionhub.repository.CommunityMembershipRepository;
import com.decisionhub.repository.DecisionBoardRepository;
import com.decisionhub.repository.UserRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {
    private final UserRepository users;
    private final DecisionBoardRepository boards;
    private final BoardVoteRepository votes;
    private final CommunityMembershipRepository memberships;

    public AnalyticsService(UserRepository users, DecisionBoardRepository boards,
                            BoardVoteRepository votes, CommunityMembershipRepository memberships) {
        this.users = users;
        this.boards = boards;
        this.votes = votes;
        this.memberships = memberships;
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard(String email) {
        Long userId = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found")).getId();
        Map<String, Long> categories = new LinkedHashMap<>();
        boards.countByCategory().forEach(row -> categories.put((String) row[0], (Long) row[1]));
        return new DashboardResponse(boards.countByOwner_Id(userId), votes.countByUser_Id(userId),
                memberships.countByUser_Id(userId), categories);
    }
}
