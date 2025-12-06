CREATE TABLE IF NOT EXISTS channel
(
    id             UUID                        NOT NULL,
    name           VARCHAR(255)                NOT NULL,
    description    VARCHAR(255)                NOT NULL,
    owner_id       UUID                        NOT NULL,
    is_private     BOOLEAN                     NOT NULL,
    password       VARCHAR(255)                NOT NULL,
    age_restricted BOOLEAN                     NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_channel PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS channel_user
(
    id         UUID NOT NULL,
    channel_id UUID NOT NULL,
    user_id    UUID NOT NULL,
    role_id    UUID NOT NULL,
    CONSTRAINT pk_channel_user PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS choice
(
    id          UUID                        NOT NULL,
    creator_id  UUID                        NOT NULL,
    channel_id  UUID                        NOT NULL,
    title       VARCHAR(300),
    description TEXT,
    image_link  VARCHAR(255),
    personal    BOOLEAN,
    status      VARCHAR(255),
    deadline    TIMESTAMP WITHOUT TIME ZONE,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_choice PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS choice_option
(
    id        UUID         NOT NULL,
    choice_id UUID         NOT NULL,
    name      VARCHAR(255) NOT NULL,
    position  INTEGER      NOT NULL,
    CONSTRAINT pk_choice_option PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS expert_application
(
    id           UUID                        NOT NULL,
    user_id      UUID                        NOT NULL,
    motivation   VARCHAR(255),
    status       VARCHAR(255)                NOT NULL,
    submitted_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_expert_application PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS expert_profile
(
    id           UUID NOT NULL,
    user_id      UUID NOT NULL,
    is_incognito BOOLEAN,
    price        INTEGER,
    rating       FLOAT,
    CONSTRAINT pk_expert_profile PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS "group"
(
    id         UUID                        NOT NULL,
    name       VARCHAR(255)                NOT NULL,
    owner_id   UUID                        NOT NULL,
    parent_id  UUID,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_group PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS group_user
(
    id       UUID NOT NULL,
    group_id UUID,
    user_id  UUID,
    CONSTRAINT pk_group_user PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS payment
(
    id           UUID           NOT NULL,
    amount       DECIMAL(19, 2) NOT NULL,
    currency     VARCHAR(3)     NOT NULL,
    status       VARCHAR(255)   NOT NULL,
    description  VARCHAR(255),
    client_email VARCHAR(255),
    metadata     JSONB,
    created_at   TIMESTAMP WITHOUT TIME ZONE,
    updated_at   TIMESTAMP WITHOUT TIME ZONE,
    paid_at      TIMESTAMP WITHOUT TIME ZONE,
    cancelled_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_payment PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS role
(
    id   UUID         NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(255) NOT NULL,
    CONSTRAINT pk_role PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS stack
(
    id          UUID NOT NULL,
    title       VARCHAR(255),
    description TEXT,
    owner_id    UUID NOT NULL,
    is_quiz     BOOLEAN,
    published   BOOLEAN,
    created_at  TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_stack PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS stack_item
(
    id        UUID NOT NULL,
    stack_id  UUID,
    choice_id UUID,
    position  INTEGER,
    CONSTRAINT pk_stack_item PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS "user"
(
    id         UUID                        NOT NULL,
    phone      VARCHAR(255),
    login      VARCHAR(255)                NOT NULL,
    password   VARCHAR(255)                NOT NULL,
    email      VARCHAR(255),
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    birthday   TIMESTAMP WITHOUT TIME ZONE,
    language   VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS vote
(
    id        UUID                        NOT NULL,
    choice_id UUID                        NOT NULL,
    option_id UUID                        NOT NULL,
    user_id   UUID                        NOT NULL,
    voted_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_vote PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS session
(
    id              UUID PRIMARY KEY,
    jwt             TEXT,
    user_id         UUID,
    create_datetime TIMESTAMP WITH TIME ZONE,
    end_datetime    TIMESTAMP WITH TIME ZONE
);

ALTER TABLE role
    ADD CONSTRAINT uc_role_code UNIQUE (code);

ALTER TABLE "user"
    ADD CONSTRAINT uc_user_login UNIQUE (login);

ALTER TABLE channel_user
    ADD CONSTRAINT uk_channel_user UNIQUE (channel_id, user_id);

ALTER TABLE channel
    ADD CONSTRAINT FK_CHANNEL_ON_OWNER FOREIGN KEY (owner_id) REFERENCES "user" (id);

ALTER TABLE channel_user
    ADD CONSTRAINT FK_CHANNEL_USER_ON_CHANNEL FOREIGN KEY (channel_id) REFERENCES channel (id);

ALTER TABLE channel_user
    ADD CONSTRAINT FK_CHANNEL_USER_ON_ROLE FOREIGN KEY (role_id) REFERENCES role (id);

ALTER TABLE channel_user
    ADD CONSTRAINT FK_CHANNEL_USER_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (id);

ALTER TABLE choice
    ADD CONSTRAINT FK_CHOICE_ON_CHANNEL FOREIGN KEY (channel_id) REFERENCES channel (id);

ALTER TABLE choice
    ADD CONSTRAINT FK_CHOICE_ON_CREATOR FOREIGN KEY (creator_id) REFERENCES "user" (id);

ALTER TABLE choice_option
    ADD CONSTRAINT FK_CHOICE_OPTION_ON_CHOICE FOREIGN KEY (choice_id) REFERENCES choice (id);

ALTER TABLE expert_application
    ADD CONSTRAINT FK_EXPERT_APPLICATION_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (id);

ALTER TABLE expert_profile
    ADD CONSTRAINT FK_EXPERT_PROFILE_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (id);

ALTER TABLE "group"
    ADD CONSTRAINT FK_GROUP_ON_OWNER FOREIGN KEY (owner_id) REFERENCES "user" (id);

ALTER TABLE "group"
    ADD CONSTRAINT FK_GROUP_ON_PARENT FOREIGN KEY (parent_id) REFERENCES "group" (id);

ALTER TABLE group_user
    ADD CONSTRAINT FK_GROUP_USER_ON_GROUP FOREIGN KEY (group_id) REFERENCES "group" (id);

ALTER TABLE group_user
    ADD CONSTRAINT FK_GROUP_USER_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (id);

ALTER TABLE stack_item
    ADD CONSTRAINT FK_STACK_ITEM_ON_CHOICE FOREIGN KEY (choice_id) REFERENCES choice (id);

ALTER TABLE stack_item
    ADD CONSTRAINT FK_STACK_ITEM_ON_STACK FOREIGN KEY (stack_id) REFERENCES stack (id);

ALTER TABLE stack
    ADD CONSTRAINT FK_STACK_ON_OWNER FOREIGN KEY (owner_id) REFERENCES "user" (id);

ALTER TABLE vote
    ADD CONSTRAINT FK_VOTE_ON_CHOICE FOREIGN KEY (choice_id) REFERENCES choice (id);

ALTER TABLE vote
    ADD CONSTRAINT FK_VOTE_ON_OPTION FOREIGN KEY (option_id) REFERENCES choice_option (id);

ALTER TABLE vote
    ADD CONSTRAINT FK_VOTE_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (id);