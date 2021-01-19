DataSpread is under active development. 
This guide contains information on the workflow for contributing to the Dataspread codebase. 
Parts of this guide are adapted from the [Contributing Document](https://github.com/lux-org/lux/blob/master/CONTRIBUTING.md) 
of [Lux](https://github.com/lux-org/lux)

# Fork a Local Repo 

To setup Dataspread manually for development purposes, you should [fork](https://docs.github.com/en/github/getting-started-with-github/fork-a-repo) the Github repo and clone the forked version.

```bash
git clone https://github.com/USERNAME/dataspread-web.git
```

# Submitting a Pull Request

You can commit your code and push to your forked repo. 
Once all of your local changes have been tested and formatted, 
you are ready to submit a pull request (PR). 
For Dataspread, we use the "Squash and Merge" strategy to merge in PR, 
which means that even if you make a lot of small commits in your PR, 
they will all get squashed into a single commit associated with the PR. 
Please make sure that comments and unnecessary file changes are not committed as part of the PR 
by looking at the "File Changes" diff view on the pull request page.
    
Once the pull request is submitted, the maintainer will get notified and review your pull request. 
They may ask for additional changes or comment on the PR. 
You can always make updates to your pull request after submitting it.
