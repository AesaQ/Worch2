CREATE UNIQUE INDEX ux_vote_user_choice
    ON vote (user_id, choice_id);