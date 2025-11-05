CREATE TABLE access_pass (
    booking_id bigint NOT NULL,
    code_hash text NOT NULL,
    valid_from timestamp with time zone NOT NULL,
    valid_to timestamp with time zone NOT NULL,
    is_active boolean DEFAULT false,
    CONSTRAINT access_pass_check CHECK ((valid_to > valid_from))
);





CREATE TABLE audit_log (
    id bigint NOT NULL,
    actor_id bigint,
    action text NOT NULL,
    payload jsonb,
    created_at timestamp with time zone DEFAULT now()
);





CREATE TABLE booking (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    space_id bigint NOT NULL,
    starts_at timestamp with time zone NOT NULL,
    ends_at timestamp with time zone NOT NULL,
    status booking_status DEFAULT 'DRAFT'::booking_status NOT NULL,
    total_cents bigint DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT now(),
    CONSTRAINT booking_check CHECK ((ends_at > starts_at))
);





CREATE TABLE booking_equipment (
    booking_id bigint NOT NULL,
    equipment_id bigint NOT NULL,
    qty integer NOT NULL,
    CONSTRAINT booking_equipment_qty_check CHECK ((qty > 0))
);





CREATE TABLE company (
    id bigint NOT NULL,
    name text NOT NULL,
    tax_id text,
    balance_cents bigint DEFAULT 0,
    created_at timestamp with time zone DEFAULT now()
);





CREATE TABLE equipment (
    id bigint NOT NULL,
    name text NOT NULL
);





CREATE TABLE incident (
    id bigint NOT NULL,
    booking_id bigint,
    user_id bigint,
    type text NOT NULL,
    penalty_cents bigint DEFAULT 0,
    created_at timestamp with time zone DEFAULT now(),
    details text,
    CONSTRAINT incident_penalty_cents_check CHECK ((penalty_cents >= 0)),
    CONSTRAINT incident_type_check CHECK ((type = ANY (ARRAY['NO_SHOW'::text, 'LATE_LEAVE'::text, 'NOISE'::text, 'OTHER'::text])))
);





CREATE TABLE invoice (
    id bigint NOT NULL,
    booking_id bigint,
    issued_at timestamp with time zone DEFAULT now(),
    amount_cents bigint NOT NULL,
    CONSTRAINT invoice_amount_cents_check CHECK ((amount_cents >= 0))
);





CREATE TABLE location (
    id bigint NOT NULL,
    name text NOT NULL,
    address text NOT NULL
);





CREATE TABLE membership (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    company_id bigint NOT NULL,
    role_in_company text,
    monthly_budget_cents bigint
);





CREATE TABLE payment (
    id bigint NOT NULL,
    invoice_id bigint,
    status payment_status DEFAULT 'NEW'::payment_status NOT NULL,
    provider text,
    provider_ref text,
    amount_cents bigint NOT NULL,
    created_at timestamp with time zone DEFAULT now(),
    CONSTRAINT payment_amount_cents_check CHECK ((amount_cents >= 0))
);





CREATE TABLE price_rule (
    id bigint NOT NULL,
    tariff_plan_id bigint NOT NULL,
    rule_type text NOT NULL,
    dow_mask bit(7),
    time_from time without time zone,
    time_to time without time zone,
    percent integer,
    promo_code text,
    CONSTRAINT price_rule_rule_type_check CHECK ((rule_type = ANY (ARRAY['PEAK'::text, 'DISCOUNT'::text, 'PROMO'::text])))
);





CREATE TABLE role (
    id smallint NOT NULL,
    code text NOT NULL
);





CREATE TABLE space (
    id bigint NOT NULL,
    location_id bigint NOT NULL,
    name text NOT NULL,
    capacity integer NOT NULL,
    type text NOT NULL,
    tariff_plan_id bigint,
    is_active boolean DEFAULT true,
    CONSTRAINT space_capacity_check CHECK ((capacity > 0)),
    CONSTRAINT space_type_check CHECK ((type = ANY (ARRAY['OPEN_DESK'::text, 'MEETING_ROOM'::text])))
);





CREATE TABLE space_equipment (
    space_id bigint NOT NULL,
    equipment_id bigint NOT NULL,
    qty integer DEFAULT 1 NOT NULL,
    CONSTRAINT space_equipment_qty_check CHECK ((qty > 0))
);





CREATE TABLE tariff_plan (
    id bigint NOT NULL,
    name text NOT NULL,
    currency text DEFAULT 'RUB'::text NOT NULL,
    base_price_cph integer NOT NULL,
    cancel_policy jsonb DEFAULT '{}'::jsonb,
    CONSTRAINT tariff_plan_base_price_cph_check CHECK ((base_price_cph > 0))
);





CREATE TABLE user_account (
    id bigint NOT NULL,
    email public.citext NOT NULL,
    password_hash text NOT NULL,
    full_name text NOT NULL,
    phone text,
    created_at timestamp with time zone DEFAULT now(),
    is_active boolean DEFAULT true
);





CREATE TABLE user_role (
    user_id bigint NOT NULL,
    role_id smallint NOT NULL
);







ALTER TABLE ONLY audit_log ALTER COLUMN id SET DEFAULT nextval('audit_log_id_seq'::regclass);




ALTER TABLE ONLY booking ALTER COLUMN id SET DEFAULT nextval('booking_id_seq'::regclass);




ALTER TABLE ONLY company ALTER COLUMN id SET DEFAULT nextval('company_id_seq'::regclass);




ALTER TABLE ONLY equipment ALTER COLUMN id SET DEFAULT nextval('equipment_id_seq'::regclass);




ALTER TABLE ONLY incident ALTER COLUMN id SET DEFAULT nextval('incident_id_seq'::regclass);




ALTER TABLE ONLY invoice ALTER COLUMN id SET DEFAULT nextval('invoice_id_seq'::regclass);




ALTER TABLE ONLY location ALTER COLUMN id SET DEFAULT nextval('location_id_seq'::regclass);




ALTER TABLE ONLY membership ALTER COLUMN id SET DEFAULT nextval('membership_id_seq'::regclass);




ALTER TABLE ONLY payment ALTER COLUMN id SET DEFAULT nextval('payment_id_seq'::regclass);




ALTER TABLE ONLY price_rule ALTER COLUMN id SET DEFAULT nextval('price_rule_id_seq'::regclass);




ALTER TABLE ONLY role ALTER COLUMN id SET DEFAULT nextval('role_id_seq'::regclass);




ALTER TABLE ONLY space ALTER COLUMN id SET DEFAULT nextval('space_id_seq'::regclass);




ALTER TABLE ONLY tariff_plan ALTER COLUMN id SET DEFAULT nextval('tariff_plan_id_seq'::regclass);




ALTER TABLE ONLY user_account ALTER COLUMN id SET DEFAULT nextval('user_account_id_seq'::regclass);




ALTER TABLE ONLY access_pass
    ADD CONSTRAINT access_pass_pkey PRIMARY KEY (booking_id);




ALTER TABLE ONLY audit_log
    ADD CONSTRAINT audit_log_pkey PRIMARY KEY (id);




ALTER TABLE ONLY booking_equipment
    ADD CONSTRAINT booking_equipment_pkey PRIMARY KEY (booking_id, equipment_id);




ALTER TABLE ONLY booking
    ADD CONSTRAINT booking_pkey PRIMARY KEY (id);




ALTER TABLE ONLY company
    ADD CONSTRAINT company_name_key UNIQUE (name);




ALTER TABLE ONLY company
    ADD CONSTRAINT company_pkey PRIMARY KEY (id);




ALTER TABLE ONLY equipment
    ADD CONSTRAINT equipment_name_key UNIQUE (name);




ALTER TABLE ONLY equipment
    ADD CONSTRAINT equipment_pkey PRIMARY KEY (id);




ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_pkey PRIMARY KEY (id);




ALTER TABLE ONLY invoice
    ADD CONSTRAINT invoice_booking_id_key UNIQUE (booking_id);




ALTER TABLE ONLY invoice
    ADD CONSTRAINT invoice_pkey PRIMARY KEY (id);




ALTER TABLE ONLY location
    ADD CONSTRAINT location_pkey PRIMARY KEY (id);




ALTER TABLE ONLY membership
    ADD CONSTRAINT membership_pkey PRIMARY KEY (id);




ALTER TABLE ONLY membership
    ADD CONSTRAINT membership_user_id_company_id_key UNIQUE (user_id, company_id);




ALTER TABLE ONLY payment
    ADD CONSTRAINT payment_pkey PRIMARY KEY (id);




ALTER TABLE ONLY price_rule
    ADD CONSTRAINT price_rule_pkey PRIMARY KEY (id);




ALTER TABLE ONLY role
    ADD CONSTRAINT role_code_key UNIQUE (code);




ALTER TABLE ONLY role
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);




ALTER TABLE ONLY space_equipment
    ADD CONSTRAINT space_equipment_pkey PRIMARY KEY (space_id, equipment_id);




ALTER TABLE ONLY space
    ADD CONSTRAINT space_pkey PRIMARY KEY (id);




ALTER TABLE ONLY tariff_plan
    ADD CONSTRAINT tariff_plan_name_key UNIQUE (name);




ALTER TABLE ONLY tariff_plan
    ADD CONSTRAINT tariff_plan_pkey PRIMARY KEY (id);




ALTER TABLE ONLY user_account
    ADD CONSTRAINT user_account_email_key UNIQUE (email);




ALTER TABLE ONLY user_account
    ADD CONSTRAINT user_account_pkey PRIMARY KEY (id);




ALTER TABLE ONLY user_role
    ADD CONSTRAINT user_role_pkey PRIMARY KEY (user_id, role_id);




CREATE INDEX idx_access_valid ON access_pass USING btree (valid_from, valid_to, is_active);




CREATE INDEX idx_booking_space_time ON booking USING btree (space_id, starts_at, ends_at);




CREATE INDEX idx_booking_user ON booking USING btree (user_id, starts_at DESC);




CREATE INDEX idx_incident_user ON incident USING btree (user_id, created_at DESC);




CREATE INDEX idx_invoice_booking ON invoice USING btree (booking_id);




CREATE INDEX idx_payment_invoice ON payment USING btree (invoice_id, status);




CREATE INDEX idx_space_location ON space USING btree (location_id, is_active);




CREATE TRIGGER trg_booking_status AFTER UPDATE OF status ON booking FOR EACH ROW EXECUTE FUNCTION booking_status_flow();




CREATE TRIGGER trg_no_overlap BEFORE INSERT OR UPDATE ON booking FOR EACH ROW EXECUTE FUNCTION ensure_no_overlap();




CREATE TRIGGER trg_set_total BEFORE INSERT OR UPDATE OF space_id, starts_at, ends_at ON booking FOR EACH ROW EXECUTE FUNCTION set_booking_total();




ALTER TABLE ONLY access_pass
    ADD CONSTRAINT access_pass_booking_id_fkey FOREIGN KEY (booking_id) REFERENCES booking(id) ON DELETE CASCADE;
ALTER TABLE ONLY booking_equipment
    ADD CONSTRAINT booking_equipment_booking_id_fkey FOREIGN KEY (booking_id) REFERENCES booking(id) ON DELETE CASCADE;
ALTER TABLE ONLY booking_equipment
    ADD CONSTRAINT booking_equipment_equipment_id_fkey FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE RESTRICT;
ALTER TABLE ONLY booking
    ADD CONSTRAINT booking_space_id_fkey FOREIGN KEY (space_id) REFERENCES space(id);
ALTER TABLE ONLY booking
    ADD CONSTRAINT booking_user_id_fkey FOREIGN KEY (user_id) REFERENCES user_account(id);
ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_booking_id_fkey FOREIGN KEY (booking_id) REFERENCES booking(id) ON DELETE SET NULL;
ALTER TABLE ONLY incident
    ADD CONSTRAINT incident_user_id_fkey FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE SET NULL;
ALTER TABLE ONLY invoice
    ADD CONSTRAINT invoice_booking_id_fkey FOREIGN KEY (booking_id) REFERENCES booking(id) ON DELETE CASCADE;
ALTER TABLE ONLY membership
    ADD CONSTRAINT membership_company_id_fkey FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE;
ALTER TABLE ONLY membership
    ADD CONSTRAINT membership_user_id_fkey FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE;
ALTER TABLE ONLY payment
    ADD CONSTRAINT payment_invoice_id_fkey FOREIGN KEY (invoice_id) REFERENCES invoice(id) ON DELETE CASCADE;
ALTER TABLE ONLY price_rule
    ADD CONSTRAINT price_rule_tariff_plan_id_fkey FOREIGN KEY (tariff_plan_id) REFERENCES tariff_plan(id) ON DELETE CASCADE;
ALTER TABLE ONLY space_equipment
    ADD CONSTRAINT space_equipment_equipment_id_fkey FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE CASCADE;
ALTER TABLE ONLY space_equipment
    ADD CONSTRAINT space_equipment_space_id_fkey FOREIGN KEY (space_id) REFERENCES space(id) ON DELETE CASCADE;
ALTER TABLE ONLY space
    ADD CONSTRAINT space_location_id_fkey FOREIGN KEY (location_id) REFERENCES location(id) ON DELETE CASCADE;
ALTER TABLE ONLY space
    ADD CONSTRAINT space_tariff_plan_id_fkey FOREIGN KEY (tariff_plan_id) REFERENCES tariff_plan(id);
ALTER TABLE ONLY user_role
    ADD CONSTRAINT user_role_role_id_fkey FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE;
ALTER TABLE ONLY user_role
    ADD CONSTRAINT user_role_user_id_fkey FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE;
