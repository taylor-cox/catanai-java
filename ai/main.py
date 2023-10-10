from agent_trainer.game_trainer import GameTrainer
from pyinstrument import Profiler

PROFILING: bool = False

if __name__ == '__main__':
    if PROFILING:
        profiler = Profiler()
        profiler.start()

        trainer = GameTrainer(True, profiling=PROFILING)

        profiler.stop()
        profiler.print()
    else:
        trainer = GameTrainer(True)


