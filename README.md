# jaca-ddm

JaCa-DDM is a Distributed Data Mining (DDM) tool based on the Agents & Artifacts paradigm as implemented in Jason and CArtAgO. It is intended to design, implement and evaluate **learning strategies**, i.e.,  encapsulated workflows modeling the interactions of agents in an environment composed of Weka tools and data sources, with the objective of creating a classification model.

Learning strategies are expected to be applicable in any DDM setting, the deployment details being managed by the JaCa-DDM platform. In this sense, learning strategies are plug-and-play. The JaCa-DDM distribution already includes a set of predefined learning strategies, being also possible to add new ones.

JaCa-DDM considers any kind of environment where data is split in various sites, even geographically distributed, as a DDM setting. With JaCa-DDM is possible to configure and deploy a DDM system that takes into account the different sites and their data. The actual data mining process is encapsulated on the strategy, which might have some configurable parameters that can be set as part of the general configuration.

JaCa-DDM is a tool for experimenting with different DDM approaches, as it evaluates the produced classification model, yielding various performance statistics (total time, classification accuracy, network traffic produced, model complexity, confusion matrix).

JaCa-DDM can be extended through the adding of new learning strategies and artifacts. Artifacts are first-class entities in the agent environment that encapsulate services, in the case of JaCa-DDM, these services consists on Weka related tools.

## Software requirements

- Java >= 1.8.
- CUDA 7 or greater (only for GPU-based strategies).
- Python 2 or Python 3 for running experiments on headless mode.

## Quick installation

JaCa-DDM includes three directories:

- node0. It contains the binaries, libraries, and source code related to the computer controlling the experiments. It can be though as the main program, launching the graphic user interface of JaCa-DDM. In a Unix like system you must execute in this directory:

'''
> run.sh
'''

This launches the JaCa-DDM gui:

![Jaca-DDM gui](/img/jaca-ddm-gui.png)
