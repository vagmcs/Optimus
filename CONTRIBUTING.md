# Contributing to Optimus

Do you like Optimus and want to get involved? Cool! That is wonderful!

Please take a moment to review this document, in order to make the contribution process easy and effective for everyone
involved.

## Core Ideas

The purpose of Optimus is to provide both a usable tool and a library for linear and quadratic mathematical optimization
using high level mathematical modeling (algebraic expressions) to describe an optimization problem. Optimus supports a
variety of optimization settings by using existing mathematical programming solvers. As a tool it aims to provide useful
features around optimization that are mature enough in terms of usability and stability. We prefer to postpone the release
of a feature, in order to have implementations that are clean in terms of user experience and development friendliness,
well-documented (documentation and examples) and well-tested (unit tests, example code).

There are two main branches, master and develop. The former, contains the stable versions of Optimus, thus it is not related
to active development version, pull requests and hot-fixes. The code in master branch is considered as frozen. Even in
situations of hot-fixes or minor improvements we prefer to fix them in the development version first. The latter,
develop branch, contains the latest development snapshot of Optimus. We strongly suggest to work your contributions over
the develop branch.

## Submitting a Pull Request

Good pull requests, such as patches, improvements, and new features, are a fantastic help. They should remain focused
in scope and avoid containing unrelated commits.

Please **ask first** if somebody else is already working on this or the core developers think your feature is in-scope
for Optimus. Generally always have a related issue with discussions for whatever you are including.

Please also provide a test plan, i.e., specify how you verified that your addition works, add unit tests or provide
examples.

Finally, since master branch is only for stable releases tagged with a version, **a pull request should be always target
to the develop branch.**

Thank you again for considering to contribute to Optimus and happy hacking :)
