import os
import numpy as np
import torch as T
import torch.nn as nn
import torch.nn.functional as F
import torch.optim as optim
from gymnasium.spaces import Discrete, MultiDiscrete
from typing import Tuple


class DQN(nn.Module):
    def __init__(
        self,
        learning_rate: float,
        n_actions: int,
        input_dims: Tuple[int],
        chkpt_dir: str,
        player_id: str,
        name: str,
        categories=[]
    ):
        super(DQN, self).__init__()

        self.checkpoint_dir = chkpt_dir
        self.name = name

        self.model = nn.Sequential(
            nn.Linear(*input_dims, 1024),
            nn.ReLU(),
            nn.Linear(1024, 512),
            nn.ReLU(),
            nn.Linear(512, 512),
            nn.ReLU(),
            nn.Linear(512, n_actions)
        )

        self.optimizer = optim.Adam(self.parameters(), lr=learning_rate)
        self.loss = nn.SmoothL1Loss()
        self.device = T.device('cuda:0' if T.cuda.is_available() else 'cpu')
        self.to(self.device)

    def forward(self, state):
        return self.model(state)

    def save_checkpoint(self, player_id: str):
        T.save(self.state_dict(),
               f'{self.checkpoint_dir}{self.name}_{player_id}.ckpt')

    def load_checkpoint(self, player_id: str):
        self.load_state_dict(
            T.load(f'{self.checkpoint_dir}{self.name}_{player_id}.ckpt'))
