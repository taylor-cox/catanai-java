--
-- PostgreSQL database dump
--

-- Dumped from database version 15.4 (Debian 15.4-1.pgdg120+1)
-- Dumped by pg_dump version 15.4 (Debian 15.4-1.pgdg120+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: games; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA games;


ALTER SCHEMA games OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: actions; Type: TABLE; Schema: games; Owner: postgres
--

CREATE TABLE games.actions (
    id smallint NOT NULL,
    action text NOT NULL
);


ALTER TABLE games.actions OWNER TO postgres;

--
-- Name: actions_id_seq; Type: SEQUENCE; Schema: games; Owner: postgres
--

CREATE SEQUENCE games.actions_id_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE games.actions_id_seq OWNER TO postgres;

--
-- Name: actions_id_seq; Type: SEQUENCE OWNED BY; Schema: games; Owner: postgres
--

ALTER SEQUENCE games.actions_id_seq OWNED BY games.actions.id;


--
-- Name: development_cards; Type: TABLE; Schema: games; Owner: postgres
--

CREATE TABLE games.development_cards (
    id bigint NOT NULL,
    name text NOT NULL
);


ALTER TABLE games.development_cards OWNER TO postgres;

--
-- Name: development_cards_id_seq; Type: SEQUENCE; Schema: games; Owner: postgres
--

CREATE SEQUENCE games.development_cards_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE games.development_cards_id_seq OWNER TO postgres;

--
-- Name: development_cards_id_seq; Type: SEQUENCE OWNED BY; Schema: games; Owner: postgres
--

ALTER SEQUENCE games.development_cards_id_seq OWNED BY games.development_cards.id;


--
-- Name: gamestates; Type: TABLE; Schema: games; Owner: postgres
--

CREATE TABLE games.gamestates (
    id bigint NOT NULL,
    game_id integer NOT NULL,
    nodes smallint[] NOT NULL,
    edges smallint[] NOT NULL,
    tiles smallint[] NOT NULL,
    banks smallint[] NOT NULL,
    "playerMetadata" smallint[] NOT NULL,
    "playerFullResourceCards" smallint[] NOT NULL,
    "playerPerspectiveResourceCards" smallint[] NOT NULL,
    ports smallint[] NOT NULL,
    "lastRoll" smallint NOT NULL,
    "currentPlayer" smallint NOT NULL,
    "actionID" smallint NOT NULL,
    "numAttemptedActionsBeforeSuccessful" bigint,
    reward numeric NOT NULL,
    "playerDevelopmentCards" smallint[] NOT NULL,
    "actionMetadata" smallint[] NOT NULL,
    "robberIndex" smallint NOT NULL,
    agent smallint NOT NULL,
    CONSTRAINT gamestates_banks_check CHECK (((array_ndims(banks) = 1) AND (array_length(banks, 1) = 6))),
    CONSTRAINT gamestates_edges_check CHECK (((array_ndims(edges) = 1) AND (array_length(edges, 1) = 72))),
    CONSTRAINT gamestates_nodes_check CHECK (((array_ndims(nodes) = 2) AND (array_length(nodes, 1) = 54) AND (array_length(nodes, 2) = 2))),
    CONSTRAINT "gamestates_playerFullResourceCards_check" CHECK (((array_ndims("playerFullResourceCards") = 2) AND (array_length("playerFullResourceCards", 1) = 4) AND (array_length("playerFullResourceCards", 2) = 5))),
    CONSTRAINT "gamestates_playerMetadata_check" CHECK (((array_ndims("playerMetadata") = 2) AND (array_length("playerMetadata", 1) = 4))),
    CONSTRAINT "gamestates_playerPerspectiveResourceCards_check" CHECK (((array_ndims("playerPerspectiveResourceCards") = 2) AND (array_length("playerPerspectiveResourceCards", 1) = 4) AND (array_length("playerPerspectiveResourceCards", 2) = 5))),
    CONSTRAINT gamestates_ports_check CHECK (((array_ndims(ports) = 1) AND (array_length(ports, 1) = 9))),
    CONSTRAINT gamestates_tiles_check CHECK (((array_ndims(tiles) = 2) AND (array_length(tiles, 1) = 19)))
);


ALTER TABLE games.gamestates OWNER TO postgres;

--
-- Name: gamestates_id_seq; Type: SEQUENCE; Schema: games; Owner: postgres
--

CREATE SEQUENCE games.gamestates_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE games.gamestates_id_seq OWNER TO postgres;

--
-- Name: gamestates_id_seq; Type: SEQUENCE OWNED BY; Schema: games; Owner: postgres
--

ALTER SEQUENCE games.gamestates_id_seq OWNED BY games.gamestates.id;


--
-- Name: resource_cards; Type: TABLE; Schema: games; Owner: postgres
--

CREATE TABLE games.resource_cards (
    id smallint NOT NULL,
    name text NOT NULL
);


ALTER TABLE games.resource_cards OWNER TO postgres;

--
-- Name: order_of_cards_id_seq; Type: SEQUENCE; Schema: games; Owner: postgres
--

CREATE SEQUENCE games.order_of_cards_id_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE games.order_of_cards_id_seq OWNER TO postgres;

--
-- Name: order_of_cards_id_seq; Type: SEQUENCE OWNED BY; Schema: games; Owner: postgres
--

ALTER SEQUENCE games.order_of_cards_id_seq OWNED BY games.resource_cards.id;


--
-- Name: actions id; Type: DEFAULT; Schema: games; Owner: postgres
--

ALTER TABLE ONLY games.actions ALTER COLUMN id SET DEFAULT nextval('games.actions_id_seq'::regclass);


--
-- Name: development_cards id; Type: DEFAULT; Schema: games; Owner: postgres
--

ALTER TABLE ONLY games.development_cards ALTER COLUMN id SET DEFAULT nextval('games.development_cards_id_seq'::regclass);


--
-- Name: gamestates id; Type: DEFAULT; Schema: games; Owner: postgres
--

ALTER TABLE ONLY games.gamestates ALTER COLUMN id SET DEFAULT nextval('games.gamestates_id_seq'::regclass);


--
-- Name: resource_cards id; Type: DEFAULT; Schema: games; Owner: postgres
--

ALTER TABLE ONLY games.resource_cards ALTER COLUMN id SET DEFAULT nextval('games.order_of_cards_id_seq'::regclass);


--
-- Data for Name: actions; Type: TABLE DATA; Schema: games; Owner: postgres
--

COPY games.actions (id, action) FROM stdin;
0       NEW GAME
1       PLAY ROAD
2       PLAY SETTLEMENT
3       PLAY CITY
4       PLAY KNIGHT
5       PLAY ROAD BUILDING
6       PLAY YEAR OF PLENTY
7       PLAY MONOPOLY
8       DRAW DEVELOPMENT CARD
9       OFFER TRADE
10      ACCEPT TRADE
11      DECLINE TRADE
12      MOVE ROBBER
13      DISCARD
14      END TURN
15      ROLL_DICE
\.


--
-- Data for Name: development_cards; Type: TABLE DATA; Schema: games; Owner: postgres
--

COPY games.development_cards (id, name) FROM stdin;
1       Knight
2       Road Building
3       Year of Plenty
4       Monopoly
5       Victory Point
\.


--
-- Data for Name: gamestates; Type: TABLE DATA; Schema: games; Owner: postgres
--

COPY games.gamestates (id, game_id, nodes, edges, tiles, banks, "playerMetadata", "playerFullResourceCards", "playerPerspectiveResourceCards", ports, "lastRoll", "currentPlayer", "actionID", "numAttemptedActionsBeforeSuccessful", reward, "playerDevelopmentCards", "actionMetadata", "robberIndex", agent) FROM stdin;
\.


--
-- Data for Name: resource_cards; Type: TABLE DATA; Schema: games; Owner: postgres
--

COPY games.resource_cards (id, name) FROM stdin;
0       WOOL
1       GRAIN
2       LUMBER
3       ORE
4       BRICK
\.


--
-- Name: actions_id_seq; Type: SEQUENCE SET; Schema: games; Owner: postgres
--

SELECT pg_catalog.setval('games.actions_id_seq', 1, true);


--
-- Name: development_cards_id_seq; Type: SEQUENCE SET; Schema: games; Owner: postgres
--

SELECT pg_catalog.setval('games.development_cards_id_seq', 1, false);


--
-- Name: gamestates_id_seq; Type: SEQUENCE SET; Schema: games; Owner: postgres
--

SELECT pg_catalog.setval('games.gamestates_id_seq', 8886, true);


--
-- Name: order_of_cards_id_seq; Type: SEQUENCE SET; Schema: games; Owner: postgres
--

SELECT pg_catalog.setval('games.order_of_cards_id_seq', 1, false);


--
-- Name: actions actions_pkey; Type: CONSTRAINT; Schema: games; Owner: postgres
--

ALTER TABLE ONLY games.actions
    ADD CONSTRAINT actions_pkey PRIMARY KEY (id);


--
-- Name: development_cards development_cards_pkey; Type: CONSTRAINT; Schema: games; Owner: postgres
--

ALTER TABLE ONLY games.development_cards
    ADD CONSTRAINT development_cards_pkey PRIMARY KEY (id);


--
-- Name: gamestates gamestates_pkey; Type: CONSTRAINT; Schema: games; Owner: postgres
--

ALTER TABLE ONLY games.gamestates
    ADD CONSTRAINT gamestates_pkey PRIMARY KEY (id);


--
-- Name: resource_cards order_of_cards_pkey; Type: CONSTRAINT; Schema: games; Owner: postgres
--

ALTER TABLE ONLY games.resource_cards
    ADD CONSTRAINT order_of_cards_pkey PRIMARY KEY (id);


--
-- Name: gamestates_game_id_index; Type: INDEX; Schema: games; Owner: postgres
--

CREATE INDEX gamestates_game_id_index ON games.gamestates USING btree (game_id) WITH (deduplicate_items='true');


--
-- Name: gamestates gamestates_actionID_fkey; Type: FK CONSTRAINT; Schema: games; Owner: postgres
--

ALTER TABLE ONLY games.gamestates
    ADD CONSTRAINT "gamestates_actionID_fkey" FOREIGN KEY ("actionID") REFERENCES games.actions(id);


--
-- PostgreSQL database dump complete
--

