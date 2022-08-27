# Building and Linking Optimus

Optimus is builds for Scala 2.12, 2.13 and 3. In order to build Optimus modules (`core`, `solver-oj`, `solver-lp`, `solver-gurobi`, `solver-mosek`) from source you need to have Java 8 or higher and [SBT](http://www.scala-sbt.org/) installed in your system. Optimus build optionally depends on [Gurobi](http://www.gurobi.com/) and [Mosek](https://www.mosek.com/) solvers. In case the dependencies for Gurobi or Mosek are not included, Optimus would build a minimal version having only lp solve and oj solver.

## Instructions to build Optimus from source

**Step 1.** Optionally include Gurobi and/or Mosek library dependencies to `./lib` as illustrated in the tree:

```
lib/
|-- gurobi.jar
|-- mosek.jar
```

**Step 2.** In order to use lp solve, Gurobi and Mosek you must also set the environment variables of your system to include the solver native executables. Detailed instructions can be found in Sections [LPSolve Installation](#optional-lpsolve-installation), [Gurobi Installation](#optional-gurobi-installation) and [Mosek Installation](#optional-mosek-installation) respectively.

**Step 3.** To build the Optimus distribution type the following command:

```
$ sbt build
```

## Optional LPSolve Installation

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
export LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:${HOME}/lib/lpsolve55"
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
$ brew tap brewsci/science
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
export LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:${HOME}/lib/lpsolve55"
```

#### Install LPSolve v5.5.x to ***Microsoft Windows***
  * Download [LPSolve dev](http://sourceforge.net/projects/lpsolve/files/lpsolve/5.5.2.0/): *lp_solve_5.5.2.x_dev_win64.zip* for 64bit or *lp_solve_5.5.2.x_dev_win64.zip* for 32bit.
    * Extract the archive and keep `lpsolve55.dll` file.
  * Download LPSolve java bindings [lp_solve_5.5.2.x_java.zip](http://sourceforge.net/projcts/lpsolve/files/lpsolve/5.5.2.0/).
    * Extract the archive and keep `lpsolve55j.jar` and `lpsolve55j.dll` files.
  * Create a directory containing the `lpsolve55.dll`, `lpsolve55j.jar` and `lpsolve55j.dll` files, e.g., `C:\path\to\lpsolve55`
  * Add the directory to the PATH environment variable in your system environment variables.

## Optional Gurobi Installation
Please follow the installation instructions from the [Gurobi website](http://www.gurobi.com).

## Optional Mosek Installation
Please follow the installation instructions from the [Mosek website](http://www.mosek.com).

## Local publish Optimus
Follow **steps 1 and 2** of Section [Instructions to build Optimus from source](#instructions-to-build-optimus-from-source) to build Optimus and then publish locally Optimus modules to your Apache Ivy directory (e.g., inside ~/.ivy2/local/):

```bash
$ sbt +publishLocal
```

## Usage of Optimus through Maven Central

Optimus is published into the Maven Central. In order to link Optimus `core` module (e.g., version 3.4.3) to your [SBT](http://www.scala-sbt.org/) project, add the following dependency:

```scala
libraryDependencies += "com.github.vagmcs" %% "optimus" % "3.4.3"
```

Moreover, you can link your project to each solver module that you additionally require by adding some or all of the following dependencies:

```scala
libraryDependencies += "com.github.vagmcs" %% "optimus-solver-oj" % "3.4.3"
libraryDependencies += "com.github.vagmcs" %% "optimus-solver-lp" % "3.4.3"
libraryDependencies += "com.github.vagmcs" %% "optimus-solver-gurobi" % "3.4.3"
libraryDependencies += "com.github.vagmcs" %% "optimus-solver-mosek" % "3.4.3"
```

Likewise in an [Apache Maven](https://maven.apache.org/) pom XML file add:

```xml
<dependencies>
    <dependency>
      <groupId>com.github.vagmcs</groupId>
      <artifactId>optimus_2.12</artifactId>
      <version>3.4.3</version>
    </dependency>
    <dependency>
      <groupId>com.github.vagmcs</groupId>
      <artifactId>optimus-solver-oj_2.12</artifactId>
      <version>3.4.3</version>
    </dependency>
    <dependency>
      <groupId>com.github.vagmcs</groupId>
      <artifactId>optimus-solver-lp_2.12</artifactId>
      <version>3.4.3</version>
    </dependency>
    <dependency>
      <groupId>com.github.vagmcs</groupId>
      <artifactId>optimus-solver-gurobi_2.12</artifactId>
      <version>3.4.3</version>
    </dependency>
</dependencies>
```

or

```xml
<dependencies>
    <dependency>
      <groupId>com.github.vagmcs</groupId>
      <artifactId>optimus_2.13</artifactId>
      <version>3.4.3</version>
    </dependency>
    <dependency>
      <groupId>com.github.vagmcs</groupId>
      <artifactId>optimus-solver-oj_2.13</artifactId>
      <version>3.4.3</version>
    </dependency>
    <dependency>
      <groupId>com.github.vagmcs</groupId>
      <artifactId>optimus-solver-lp_2.13</artifactId>
      <version>3.4.3</version>
    </dependency>
    <dependency>
      <groupId>com.github.vagmcs</groupId>
      <artifactId>optimus-solver-gurobi_2.13</artifactId>
      <version>3.4.3</version>
    </dependency>
</dependencies>
```

or

```xml
<dependencies>
    <dependency>
      <groupId>com.github.vagmcs</groupId>
      <artifactId>optimus_3</artifactId>
      <version>3.4.3</version>
    </dependency>
    <dependency>
      <groupId>com.github.vagmcs</groupId>
      <artifactId>optimus-solver-oj_3</artifactId>
      <version>3.4.3</version>
    </dependency>
    <dependency>
      <groupId>com.github.vagmcs</groupId>
      <artifactId>optimus-solver-lp_3</artifactId>
      <version>3.4.3</version>
    </dependency>
    <dependency>
      <groupId>com.github.vagmcs</groupId>
      <artifactId>optimus-solver-gurobi_3</artifactId>
      <version>3.4.3</version>
    </dependency>
</dependencies>
```