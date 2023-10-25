import ray
from ray.tune.registry import register_env
from ray import tune
from ray.rllib.env import PettingZooEnv
from ray.rllib.algorithms.ppo import PPOConfig
from ray.rllib.utils import check_env

from catanenv.catanenv import CatanEnv


if __name__ == '__main__':
    ray.init()

    env_name = "CatanEnv"

    def env_creator(config): return CatanEnv('Rainbow DQN')

    register_env(env_name, lambda config: PettingZooEnv(env_creator(config)))

    check_env(PettingZooEnv(env_creator({})))

    config = (
        PPOConfig()
        .environment(env=env_name, clip_actions=True)
        .rollouts(num_rollout_workers=6, rollout_fragment_length='auto')
        .training(
            train_batch_size=512,
            lr=2e-5,
            gamma=0.99,
            grad_clip=None,
        )
        .debugging(log_level="ERROR")
        .framework(framework="torch")
        .resources(num_gpus=2, num_cpus_per_worker=2)
    )

    tune.run(
        "PPO",
        name="PPO",
        stop={"timesteps_total": 100000},
        checkpoint_freq=10,
        config=config.to_dict(),

    )
