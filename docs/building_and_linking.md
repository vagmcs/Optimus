# Building and Linking Optimus

In order to build Optimus from source you need to have Java SE version 8 or higher and [SBT](http://www.scala-sbt.org/)(v0.13.x) installed in your system. Optimus build optionally depends on [Gurobi](http://www.gurobi.com/) solver. In case the dependencies for Gurobi are not included, Optimus would build a minimal version having only lpsolve and ojAlgo.

## Instructions to build Optimus from source

**Step 1.** Optionally include Gurobi library dependencies to `./lib` as illustrated in the tree:
```
lib/
|-- gurobi.jar
```

**Step 2.** In order to use Gurobi or lpsolve you must also set the environment variables of your system to include the solver native executables. Detailed instructions can be found in Sections [LPSolve Installation](#lpsolve-installation-(optional)) and [Gurobi Installation](#gurobi-installation-(optional)).

**Step 3.** To build the Optimus distribution type the following command:
```
$ sbt dist
```

After a successful compilation, the distribution is located inside `./target/universal/optimus-<version>.zip`. The distribution contains all library dependencies and requires only Java 8 (or higher). Sources, documentation and the compiled library (without dependencies) are archived as jar files into the `./target/scala-2.11/` directory.

## LPSolve Installation (optional)

#### Install LPSolve v5.5.x to ***Debian-based*** distribution:
```bash
$ sudo apt-get install lp-solve
```

Installation of Java Native Interface support for LPSolve v5.5.x:
* Download [LPSolve dev](http://sourceforge.net/projects/lpsolve/files/lpsolve/5.5.2.0/): *lp_solve_5.5.2.x_dev_ux64.zip* for 64bit or *lp_solve_5.5.2.x_dev_ux32.zip* for 32bit.
  * Extract the archive and keep `lpsolve55.so` file.
* Download LPSolve java bindings [lp_solve_5.5.2.x_java.zip](http://sourceforge.net/projects/lpsolve/files/lpsolve/5.5.2.0/).
    * Extract the archive and keep `lpsolve55j.so` file.
* Create a directory containing the `lpsolve55.so` and `lpsolve55j.so` files, e.g., `$HOME/lib/lpsolve55`
* Add the directory to `LD_LIBRARY_PATH` in your profile:

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

#### Install LPSolve 5.5.x to ***Apple Mac OSX***
Either download and install from the [LPSolve website](http://lpsolve.sourceforge.net) or from your favorite package manager:

[Macports](https://www.macports.org):
```bash
$ sudo port install lp_solve
```

[Homebrew](http://brew.sh):
```bash
$ brew tap homebrew/science
$ brew install lp_solve
```

Installation of Java Native Interface support for LPSolve v5.5.x:
* Download [LPSolve dev](http://sourceforge.net/projects/lpsolve/files/lpsolve/5.5.2.0/): *lp_solve_5.5.2.x_dev_ux64.zip* for 64bit or *lp_solve_5.5.2.x_dev_ux32.zip* for 32bit.
  * Extract the archive and keep `lpsolve55.dylib` file.
* Download LPSolve java bindings [lp_solve_5.5.2.x_java.zip](http://sourceforge.net/projects/lpsolve/files/lpsolve/5.5.2.0/).
    * Extract the archive and keep `lpsolve55j.jnilib` file.
* Create a directory containing the `lpsolve55.dylib` and `lpsolve55j.jnilib` files, e.g., `$HOME/lib/lpsolve55`
* Add the directory to `LD_LIBRARY_PATH` inside `.profile` file in your home directory:

```bash
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$HOME/lib/lpsolve55
```

#### Install LPSolve v5.5.x to ***Microsoft Windows***
  * Download [LPSolve dev](http://sourceforge.net/projects/lpsolve/files/lpsolve/5.5.2.0/): *lp_solve_5.5.2.x_dev_win64.zip* for 64bit or *lp_solve_5.5.2.x_dev_win64.zip* for 32bit.
    * Extract the archive and keep `lpsolve55.dll` file.
  * Download LPSolve java bindings [lp_solve_5.5.2.x_java.zip](http://sourceforge.net/projcts/lpsolve/files/lpsolve/5.5.2.0/).
    * Extract the archive and keep `lpsolve55j.jar` and `lpsolve55j.dll` files.
  * Create a directory containing the `lpsolve55.dll`, `lpsolve55j.jar` and `lpsolve55j.dll` files, e.g., `C:\path\to\lpsolve55`
  * Add the directory to the PATH environment variable in your system environment variables (see [instructions](#microsoft-windows-operating-systems)).

## Gurobi Installation (optional)
Please follow the installation instructions from the [Gurobi website](http://www.gurobi.com).

## Local publish Optimus
Follow **steps 1 and 2** of Section [Instructions to build Optimus from source](#instructions-to-build-optimus-from-source) to build Optimus and then publish locally Optimus to your Apache Ivy directory (e.g., inside ~/.ivy2/local/):

```bash
$ sbt publishLocal
```

In order to link Optimus (e.g., version 1.2.2) to your [SBT](http://www.scala-sbt.org/) project, add the following dependency:

```sbt
libraryDependencies += "com.github.vagm" %% "optimus" % "1.2.2"
```

Likewise in an [Apache Maven](https://maven.apache.org/) pom xml file add:

```xml
<dependency>
    <groupId>com.github.vagm</groupId>
    <artifactId>optimus_2.11</artifactId>
    <version>1.2.2</version>
</dependency>
```