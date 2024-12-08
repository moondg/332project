# How to use gensort

## Options
+ `-a`: ascii
+ `-b`: binary(`-b` is identical to `-b0`)
+ `-bN`: starts from N-th data

## ASCII
```
gensort -a -b     1000 partition1
gensort -a -b1000 1000 partition2
gensort -a -b2000 1000 partition3
...
```

## Binary
```
gensort -b     1000 partition1
gensort -b1000 1000 partition2
gensort -b2000 1000 partition3
...
```
