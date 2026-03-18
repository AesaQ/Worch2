create table idempotency_key (
  id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         UUID         NOT NULL,
  endpoint        TEXT         NOT NULL,
  idem_key        TEXT         NOT NULL,
  response_status TEXT         NOT NULL,
  response_body   jsonb        NULL,
  created_at      timestamptz  NOT NULL DEFAULT now()
);

create unique index ux_idem_user_endpoint_key
  on idempotency_key(user_id, endpoint, idem_key);