#!/bin/bash
#SBATCH -J Main_S_1024_23
#SBATCH -p barbun
#SBATCH -N 1
#SBATCH -n 20
#SBATCH -c 1
#SBATCH --array=0-49
#SBATCH --time=3-00:00:00
#SBATCH --output=logs/out_%A_%a.txt
#SBATCH --error=logs/err_%A_%a.txt

module load lib/java/jdk-22.0.1

java Main_S_1024_23 $SLURM_ARRAY_TASK_ID
