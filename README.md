The exercises are based on these youtube video tutorials created by @mpilquist:
- https://www.youtube.com/watch?v=cahvyadYfX8&index=2&list=PLFrwDVdSrYE6PVD_p6YQLAbNaEHagx9bW&t=0s
- https://www.youtube.com/watch?v=HM0mOu5o2uA&index=3&list=PLFrwDVdSrYE6PVD_p6YQLAbNaEHagx9bW&t=0s
- https://www.youtube.com/watch?v=8YxcB6PIUDg&index=4&list=PLFrwDVdSrYE6PVD_p6YQLAbNaEHagx9bW&t=0s

The code uses the following fs2 and cats versions:
```
libraryDependencies += "co.fs2" %% "fs2-core" % "1.0.1"
libraryDependencies += "co.fs2" %% "fs2-io" % "1.0.2"
libraryDependencies += "org.typelevel" %% "cats-core" % "1.5.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "1.1.0"
```
# fs2 exercises:

### 1. "Mix" two streams:
```
def function[A](a: Stream[Pure, A], b: Stream[Pure, A]): Stream[Pure, A] = ???

function(
  Stream(1, 2, 3),
  Stream(4, 5, 6, 7, 8)
) === Stream(1, 4, 2, 5, 3, 6)
```
see [mpilquist01_pure_streams (s04)](src/main/scala/io/parseq/examples/fs2/mpilquist01_pure_streams.scala)

### 2. Delimit stream elements:
```
def function[A](a: Stream[Pure, A], delimiter: A): Stream[Pure, A] = ???

function(Stream(1, 2, 3), 0) === Stream(1, 0, 2, 0, 3)
```
see [mpilquist01_pure_streams (s05)](src/main/scala/io/parseq/examples/fs2/mpilquist01_pure_streams.scala)

### 3. Zip stream with a repeated sequence of elements (0, 1):
```
def function[A](stream: Stream[Pure, A], sequence: Seq[A]): Stream[Pure, A]

function(
  Stream(11, 22, 33, 44, 55, 66, 77),
  Seq(0, 1)
) === Stream( (11, 0),
              (22, 1),
              (33, 0),
              (44, 1),
              (55, 0),
              (66, 1),
              (77, 0) )
```
see [mpilquist01_pure_streams (s06)](src/main/scala/io/parseq/examples/fs2/mpilquist01_pure_streams.scala)

### 4. Repeatedly print out current time in millies to stdout. Limit output to the first 10 values.
see [mpilquist03_effectful_streams (s02)](src/main/scala/io/parseq/examples/fs2/mpilquist03_effectful_streams.scala)

### 5. What is the output of:
```
println( (Stream(1, 2, 3) ++ Stream(4, 5, 6)).chunks.toList )
```
see [mpilquist04_chunks (s02)](src/main/scala/io/parseq/examples/fs2/mpilquist04_chunks.scala)

### 6. What is the output of:
```
println(Stream(1, 2, 3).repeat.take(10).chunks.toList)
```
see [mpilquist04_chunks (s03)](src/main/scala/io/parseq/examples/fs2/mpilquist04_chunks.scala)

### 7. Print out current time in millies with interval of 1 second to stdout for 5 seconds.
see [mpilquist05_time](src/main/scala/io/parseq/examples/fs2/mpilquist05_time.scala)

### 8. Implement a pipe that prints out stream elements to stdout. The printed elements should have a prefix:
```
def log[A](prefix: String): Pipe[IO, A, A]
```
see [mpilquist06_concurrent (log)](src/main/scala/io/parseq/examples/fs2/mpilquist06_concurrent.scala)

### 9. Implement a pipe that delays output of a stream element with interval [0 .. maxDelay):
```
def randomDelays[A](maxDelay: FiniteDuration)(implicit ...): Pipe[IO, A, A]
```
see [mpilquist06_concurrent (randomDelays)](src/main/scala/io/parseq/examples/fs2/mpilquist06_concurrent.scala)
