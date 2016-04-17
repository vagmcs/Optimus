# Building and Linking Optimus

In order to build Optimus from source, you need to have Java SE Development Kit (e.g., OpenJDK) version 8 or higher and [SBT](http://www.scala-sbt.org/)(v0.13.x) installed in your system. Moreover, Optimus build optionally depends on [Gurobi](http://www.gurobi.com/). In case the dependencies for Gurobi are not included, Optimus would build a minimal version having only lp_solve and ojalgo.

## Instructions to build Optimus from source

**Step 1.** Optionally, include Gurobi library dependencies to `./lib`, as it is illustrated in the tree below:
```
lib/
|-- gurobi.jar
```

**Step 2.** For using Gurobi and lp_solve you should also set the environment variables of your system to make use of the solver native executable files.

**Step 3.** To build the Optimus distribution type the following command:
```
$ sbt dist
```

After a successful compilation, distribution is located inside the `./target/universal/optimus-<version>.zip` file. The distribution contains all library dependencies and requires only Java 8 (or higher). Sources, documentation and the compiled library (without dependencies) are archived as jar files into the `./target/scala-2.11/` directory.

## LPSolve installation instructions (optional)

For example, on a***Debian-based***distribution, type the following command:
```bash
$ sudo apt-get install lp-solve
```

To install Java Native Interface support for LPSolve v5.5.x you need follow the instructions below:
* Download LPSolve dev, 64bit *lp_solve_5.5.2.x_dev_ux64.zip* or for 32bit *lp_solve_5.5.2.x_dev_ux32.zip*, from [LPSolve official repository](http://sourceforge.net/projects/lpsolve/files/lpsolve/5.5.2.0/).
  * Extract the file
  * We only need the `lpsolve55.so` file.
* Download LPSolve java bindings (lp_solve_5.5.2.x_java.zip) from [LPSolve official repository](http://sourceforge.net/projects/lpsolve/files/lpsolve/5.5.2.0/).
    * Extract the file
    * We only need the `lpsolve55j.so` files
* Create a directory containing the `lpsolve55.so` and `lpsolve55j.so` files, e.g., `$HOME/lib/lpsolve55`
* Add this directory to `LD_LIBRARY_PATH` in your profile file:

**BASH** e.g., inside `.profile`, `.bashrc` or `.bash_profile` file in your home directory:
```bash
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$HOME/lib/lpsolve55
```

**CSH/TCSH** e.g., inside `~/.login` file in your home directory:
```csh
set LD_LIBRARY_PATH = ($LD_LIBRARY_PATH $HOME/lib/lpsolve55 .)
```
or in `~/.cshrc` file in your home directory:
```csh
setenv LD_LIBRARY_PATH $LD_LIBRARY_PATH:$HOME/lib/lpsolve55:.
```

### Apple MacOS X

Either download and install from the[LPSolve website](http://lpsolve.sourceforge.net)or from your favorite package manager.

[Macports](https://www.macports.org):
```bash
$ sudo port install lp_solve
```

[Homebrew](http://brew.sh):
```bash
$ brew tap homebrew/science
$ brew install lp_solve
```

To install the Java Native Interface support for LPSolve v5.5.x you need follow the  instructions below:
* Download LPSolve dev, 64bit *lp_solve_5.5.2.x_dev_ux64.zip* or for 32bit *lp_solve_5.5.2.x_dev_ux32.zip*, from [LPSolve official repository](http://sourceforge.net/projects/lpsolve/files/lpsolve/5.5.2.0/).
  * Extract the file
  * We only need the `lpsolve55.dylib` file.
* Download LPSolve java bindings (lp_solve_5.5.2.x_java.zip) from [LPSolve official repository](http://sourceforge.net/projects/lpsolve/files/lpsolve/5.5.2.0/).
    * Extract the file
    * We only need the `lpsolve55j.jnilib` files
* Create a directory containing the `lpsolve55.dylib` and `lpsolve55j.jnilib` files, e.g., `$HOME/lib/lpsolve55`
* Add this directory to `LD_LIBRARY_PATH` inside `.profile` file in your home directory:

```bash
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$HOME/lib/lpsolve55
```

### Microsoft Windows
To install LPSolve v5.5.x in your system, follow the instructions below:
  * Download LPSolve dev, 64bit *lp_solve_5.5.2.x_dev_win64.zip* or for 32bit *lp_solve_5.5.2.x_dev_win64.zip*, from [LPSolve official repository](http://sourceforge.net/projects/lpsolve/files/lpsolve/5.5.2.0/).
    * Extract the file
    * We only need the `lpsolve55.dll` file.
  * Download LPSolve java bindings (lp_solve_5.5.2.x_java.zip) from [LPSolve official repository](http://sourceforge.net/projects/lpsolve/files/lpsolve/5.5.2.0/).
    * Extract the file
    * We only need the `lpsolve55j.jar` and `lpsolve55j.dll` files
  * Create a directory containing the `lpsolve55.dll`, `lpsolve55j.jar` and `lpsolve55j.dll` files, e.g., `C:\path\to\lpsolve55`
  * Add this directory to the PATH environment variable in your system environment variables (see [instructions](#microsoft-windows-operating-systems))

## Gurobi installation instructions (optional)
Please follow the installation instructions from the [Gurobi website](http://www.gurobi.com).

## Local publish Optimus
Follow **steps 1 and 2** of Section[Instructions to build Optimus from source](#instructions-to-build-optimus-from-source)to build Optimus and then publish locally Optimus to your Apache Ivy directory (e.g., inside ~/.ivy2/local/):

```bash
$ sbt publishLocal
```

Thereafter, in order to link Optimus (e.g., version 1.2.2) to your[SBT](http://www.scala-sbt.org/)project, add the following dependency:

```sbt
libraryDependencies += "com.github.vagm" %% "optimus" % "1.2.2"
```

Similarly in an[Apache Maven](https://maven.apache.org/)pom file add:

```xml
<dependency>
    <groupId>com.github.vagm</groupId>
    <artifactId>optimus_2.11</artifactId>
    <version>1.2.2</version>
</dependency>
```