# 332project
Upload CSED332(Software Design Method) group project works

## Index
[1. Participants](#participants)  
[2. Milestone](#milestone)  
[3. Weekly Progress](#weekly-progress)  
[4. Workflow](#workflow)  
[5. Feedback](#feedback)

## Participants

[@문동균](https://github.com/moondg)  
[@배재륜](https://github.com/bjr7000)  
[@이윤혁](https://github.com/a-nodi)  

## Milestone
Refering on the _The Mythical Man-Month_, the development period was divided into 3 parts
- 1. `Design phase` (~ Week 3)
- 2. `Programming & Minor debuging phase` (~ Week 6)
- 3. `System test, obtaining all components` (~ Week 8)

### Milestone #1  
- Establish connection between master and worker  
- Design distributed sorting algorithm for this project
- Generate sample input data for testing key-value parsing  

### Milestone #2
- Complete key-value parsing
- Send and receive sample datas between master and worker

### Milestone #3
- Implement sampling
- Implement partitioning

### Milestone #4
- Implement parsing
- Implement shuffling  

### Milestone #5
- Implement sorting
- Implement mergeing

### Milestone #6
- Test integrated milestone #3 with sample data to see it works well
- Debug errors for sample data

### Milestone #7
- Test distributed sorting program with intensive data
- Debug all remaining errors for intensive data

## Weekly Progress

### Week 1

#### Progress in the week
- Convention disccusioned
- Seeking useful librarys for implementing this project
- Reading gRPC Docs


#### Goal of the next week
[@문동균](https://github.com/moondg)
- Study how to perform distribution sort
- Design key-value parsing
- Design abstact structure of sorting (Sort/Partition, Shuffle)

[@배재륜](https://github.com/bjr7000)
- Study how to perform distribution sort
- Study how to use multiple cores with scala
- Design abstact structure of sorting (Merge, Sampling)

[@이윤혁](https://github.com/a-nodi)  
- Study how to use gRPC
- Design network interaction of master and worker node

## Workflow
Workflow based on TDD (Test Driven Development)

### Commit & Pull request convention
- `Feature`: Add new function
- `Fix`: Fix bug
- `Docs`: Modify Document
- `Chore`: Change Settings (build, project configs...)
- `Test`: Add/Fix Test suite
- `Refactor`: Refactor code

### Branch convention
- `main`: For release 
- `develop`: Merge test-passed features
- `test-[TestName]`: Develop feature and testsuite

## Feedback
### Week 1 (Applied)
- Heavy Milestone #3: Milestones should have similar workloads. Milestone should take feedbacks from previous Milestone.
- No need to follow Mythical Man Month strictly: Update development cycle by merging programming phase and testing/debuging phase.
- Parsing/Sort/Partition/Shuffle, Merge/Sampling is closely related: Well-defined interface needed, extra effort for communication needed.
- TDD's idea is "test is a some kind of document": Record TDD application for docs with details.  
 