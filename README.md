# Tiny Scheduling

Applying different approaches to optimize assigning of interdependent
tasks to developers with different skill-sets and efficiencies

## Model

### Skill-set

For the sake of simplicity, only two mutually exclusive skill-sets
are considered:

* Front-end
* Back-end

All tasks and developers are assigned one of these skill-sets.

### Developer

A [developer](inego.tinyscheduling.Developer) is an executor of tasks, and has:
* Name
* Skill-set
* Efficiency (ratio to the Ideal developer)
* Leader — _optional_ reference to another developer overseeing this one.
Efficiency of the _leader_ will be lowered when she's working on
her own tasks at the time when people led by her are doing their tasks.
* Starting date — _optional_ date when the developer becomes available
to the project.

### Task

A task has:
* Name
* Type (skill-set)
* Cost (in ideal hours)
* Dependency — _optional_, a single task that has to be finished before
this task can start)
* Executor restiction — _optional_, a single exclusive developer who
can handle this task
* First flag — _optional_, to define a subset of tasks which have to
start before all other tasks

### Calendar

A calendar is an int-to-date two-way mapping support to render dates in
a plausible form (skipping weekends or holidays).

### Project

A project is a set of a calendar, developers and tasks, and serves as an
input to the optimizing algorithms.

## Goal

Given a project, successively assign tasks to developers in such a way
as to minimize the moment the last task is completed.

## Input

Currently, the input is hard-coded. See `inego/tinyscheduling/Sample.kt`.

## Approaches

## UI / Visualisation

Live computation in a Swing frame using a custom Gantt-like chart
written specifically for this project.

![Screenshot](readme/Screenshot_1.png "Screenshot")