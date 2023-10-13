from ml.dqn.agent import Agent
import logging
from typing import List, Tuple


class AgentTrainer:
    def __init__(self, player_id: str, agent: Agent | None = None):
        # Hyperparameters
        self.batch_size: int = 64
        self.gamma: float = 0.99
        self.eps_start: float = 0.95
        self.eps_end: float = 0.005
        self.eps_decay: float = 1e-6
        self.tau = 0.005
        self.learning_rate = 1e-4
        self.memory_size: int = 1_000_000
        self.n_actions: int = 239
        self.input_dims: Tuple[int] = (288,)

        # Other metrics
        self.best_score: float = 0.0
        self.score_history: List[float] = []
        self.learn_iters: int = 0
        self.avg_score: float = 0.0
        self.n_steps: int = 0
        self.score: float = 0
        self.starting_turn_counter: int = 0
        self.player_id: str = player_id
        if agent == None:
            self.agent = Agent(
                gamma=self.gamma,
                eps_start=self.eps_start,
                learning_rate=self.learning_rate,
                n_actions=self.n_actions,
                input_dims=self.input_dims,
                mem_size=self.memory_size,
                batch_size=self.batch_size,
                eps_min=self.eps_end,
                eps_dec=self.eps_decay,
                replace=1000,
                categories=[15, 87, 159, 169, 179,
                            189, 199, 209, 219, 229, 239],
                player_id=player_id,
            )
        else:
            self.agent = agent
        self.load_models()

    def reset(self):
        self.starting_turn_counter = 0

    def save_models(self):
        logging.info('Saving models.')
        if self.agent is None:
            logging.error('Agent is None.')
        self.agent.save_models(self.player_id)

    def load_models(self):
        try:
            logging.info('Loading models.')
            self.agent.load_models(self.player_id)
        except:
            pass

    def soft_update_target_network(self):
        target_network_state_dict = self.agent.target_net.state_dict()
        policy_network_state_dict = self.agent.policy_net.state_dict()
        for key in policy_network_state_dict:
            target_network_state_dict[key] = self.tau * policy_network_state_dict[key] + \
                target_network_state_dict[key] * (1 - self.tau)
