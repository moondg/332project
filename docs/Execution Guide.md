# Execution Guide
## Compiled
### master excution
```
master [number of workers] [master network port (not essential)]
```

### worker excution
```
worker [master IP:port] -I [input directory] [input directory] … [input directory] -O [output directory]
```
## Sbt run
### 1. Main
```
sbt run
1
```
This will execute functions in `Main.scala`. Usually executed for basic function check and debugging
### 2. Master
```
sbt "run N"
```
Where `N` is the number of fixed worker.   
You have to choose Master after compling. (2)   
### 3. Worker
```
sbt "run MASTER_IP:MASTER_PORT -I PATH_TO_INPUT_DIR_1 PATH_TO_INPUT_DIR_2 ... PATH_TO_INPUT_DIR_N -O PATH_TO_OUTPUT_DIR"
```
For each worker, run command above.   
You have to choose Worker after compling. (3)   
Worker should be excuted after Master starts to wait connection of Workers. (Logger shows when Master start to wait)   
Worker's output directory should be empty.   
If logger says [Merge Complete], the result file is in the output directory, named with "result".   
