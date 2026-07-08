#!/bin/bash
#SBATCH -J Main_R_4096_56
#SBATCH -p orfoz
#SBATCH -N 1
#SBATCH -n 112
#SBATCH -c 1
#SBATCH --mem-per-cpu=2G
#SBATCH --time=3-00:00:00

module load lib/java/jdk-22.0.1

java Main_R_4096_56
