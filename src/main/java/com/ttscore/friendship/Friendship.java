package com.ttscore.friendship;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friendship {

    private String id;
    private String requesterId;
    private String addresseeId;
    private FriendshipStatus status;
    private Date createdAt;
}
