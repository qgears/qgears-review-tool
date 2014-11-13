qgears-review-tool
==================

Simple tool for performing code review on large Java code basis.

# Abstract

The QGears Review Tool is to be used for reviewing source code by one or more reviewers. This tool should be used parallel to static code checkers and other heuristics to allow focusing on aspects on reviewing code that require specifically human intelligence, for example, for justifying the correnctness of complex business logic and filtering malicious changes.

# Main usage scenario

One or more source file sets may be defined on a specific branch of a source code management system (further abbreviated as 'SCM'), the files of which are the subjects to reviews. One or more reviewers may participate in reviewing the source files of these file sets.

A reviewer has to review a specific SCM revision of a file, and, as a result, has to associate a textual description and a status flag to it. The status flags and their meanings are:

* OK: the file is all right and accepted for release
* TODO: changes, corrections are required
* OFF: the file should not be the part of the file set

A reviewer may mark other, previously written reviews for a specific file as invalid, be the invalidated review or reviews created by him- or herself or other reviewers.

A review process is considered as completed for a revision if, and only if there is at least two non-obsolete, non-invalidated review with 'OK' status for every files for that revision, from at least two reviewers.

If an already reviewed file changes, i. e. a new revision of it is committed to the SCM, the reviews for that file become obsolete for the new revision, implicitly rendering the whole new revision as incomplete. In this case, the affected file has to be reviewed again by all the reviewers to reach a complete state for the new revision. Note that changing a file does not invalidate a completed review process for a previous revision.

# User interface

An Eclipse user interface helps to manage the review process with a special perspective and helps to evaluate the current status of a review process by creating statistics specific to the whole process and specific to a single reviewer.
