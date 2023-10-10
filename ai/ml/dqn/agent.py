import os
import numpy as np
import torch
import torch.nn as nn
import torch.nn.functional as F
import torch.optim as optim
from gymnasium.spaces import Discrete, MultiDiscrete
import logging
from typing import Tuple, List
from ml.dqn.dqn import DQN
import math

from ml.dqn.replay_buffer import ReplayBuffer


class Agent():
    def __init__(
        self,
        gamma: float,
        learning_rate: float,
        n_actions: int,
        input_dims: Tuple[int],
        mem_size: int,
        batch_size: int,
        player_id: str,
        eps_start: float = 0.9,
        eps_min: float = 0.01,
        eps_dec: float = 5e-7,
        replace: int = 1000,
        chkpt_dir: str = './tmp/dqn/',
        categories: List[int] = [],
    ):
        # Initialize hyperparameters
        self.gamma = gamma
        self.eps_start = eps_start
        self.learning_rate = learning_rate
        self.n_actions = n_actions
        self.input_dims = input_dims
        self.batch_size = batch_size
        self.eps_min = eps_min
        self.eps_dec = eps_dec
        self.replace_target_cnt = replace
        self.chkpt_dir = chkpt_dir
        self.categories = categories
        self.action_space = MultiDiscrete([
            15,  # Action
            72,  # Possibly holds road index
            72,  # Possibly holds road index
            10,  # Card type amount for trading
            10,  # Card type amount for trading
            10,  # Card type amount for trading
            10,  # Card type amount for trading
            10,  # Card type amount for trading
            10,  # Card type amount for trading
            10,  # Card type amount for trading
            10,  # Card type amount for trading
        ])
        self.learn_step_counter = 0
        self.steps_done = 0
        self.player_id = player_id

        # Initialize replay buffer
        self.memory = ReplayBuffer(mem_size, input_dims)

        # Initialize policy and target networks
        self.policy_net = DQN(
            self.learning_rate,
            self.n_actions,
            input_dims=self.input_dims,
            chkpt_dir=self.chkpt_dir,
            player_id=player_id,
            name='dqn'
        )

        self.target_net = DQN(
            self.learning_rate,
            self.n_actions,
            input_dims=self.input_dims,
            chkpt_dir=self.chkpt_dir,
            player_id=player_id,
            name='dqn'
        )

        self.target_net.load_state_dict(self.policy_net.state_dict())

    def choose_action(self, state):
        eps_threshold = self.eps_min + \
            (self.eps_start - self.eps_min) * \
            math.exp(-1. * self.steps_done * self.eps_dec)
        self.steps_done += 1
        if np.random.random() > eps_threshold:
            state = torch.tensor(state, dtype=torch.float,
                                 device=self.policy_net.device)
            action = self.policy_net.forward(state)
            action = np.array(action.detach().cpu())
            new_action = []
            last_category = 0
            for category in self.categories:
                new_action.append(np.argmax(action[last_category:category]))
                last_category = category
            action = new_action
        else:
            action = self.action_space.sample()

        return action

    def store_transition(self, state, action, reward, state_, done):
        self.memory.store_transition(state, action, reward, state_, done)

    def replace_target_network(self):
        if self.learn_step_counter % self.replace_target_cnt == 0:
            self.target_net.load_state_dict(self.policy_net.state_dict())

    def decrement_epsilon(self):
        self.eps_start = self.eps_start - self.eps_dec \
            if self.eps_start > self.eps_min else self.eps_min

    def save_models(self, player_id: str):
        '''
        Saves the target and policy net for the agent of the given player id.
        '''
        self.policy_net.save_checkpoint(player_id)
        self.target_net.save_checkpoint(player_id)

    def load_models(self, player_id: str):
        '''
        Loads the target and policy net for the agent of the given player id.
        '''
        self.policy_net.load_checkpoint(player_id)
        self.target_net.load_checkpoint(player_id)

    def learn(self):
        if self.memory.mem_cntr < self.batch_size:
            return

        self.policy_net.optimizer.zero_grad()
        self.replace_target_network()

        state, action, reward, new_state, done = \
            self.memory.sample_buffer(self.batch_size)

        states = torch.tensor(state).to(self.policy_net.device)
        rewards = torch.tensor(reward).to(self.policy_net.device)
        dones = torch.tensor(done).to(self.policy_net.device)
        actions = torch.tensor(action).to(self.policy_net.device)
        states_ = torch.tensor(new_state).to(self.policy_net.device)

        # non_final_mask = torch.tensor(tuple(map(lambda s: s is not None,
        #                                         dones)), device=self.policy_net.device, dtype=torch.bool)
        # non_final_mask = torch.tensor(tuple(map(lambda s: s is not None,
        #                                         dones)), device=self.policy_net.device, dtype=torch.bool).unsqueeze(1)
        non_final_mask = []
        for _ in range(self.batch_size):
            batch_final_mask = []
            for _ in range(self.n_actions):
                batch_final_mask.append(True)
            non_final_mask.append(batch_final_mask)
        non_final_mask = torch.tensor(
            non_final_mask, device=self.policy_net.device, dtype=torch.bool).reshape(self.batch_size, self.n_actions)

        state_action_values = self.policy_net.forward(
            states).gather(1, actions)

        next_state_values = torch.zeros(
            (self.batch_size, self.n_actions), device=self.policy_net.device)
        next_state_values = next_state_values[non_final_mask].reshape(
            self.batch_size, self.n_actions)

        with torch.no_grad():
            next_state_values = self.target_net.forward(
                states_).detach()

        expected_state_action_values = (
            next_state_values * self.gamma) + rewards.view(64, 1)

        loss = self.policy_net.loss(
            state_action_values, expected_state_action_values)

        self.policy_net.optimizer.zero_grad()
        loss.backward()
        torch.nn.utils.clip_grad_value_(  # type: ignore
            self.policy_net.parameters(), 1)
        self.policy_net.optimizer.step()

        self.policy_net.save_checkpoint(self.player_id)
