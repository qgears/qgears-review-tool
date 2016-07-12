qgears-review-tool
==================

Simple tool for performing code review on large Java code basis.

# Abstract

The QGears Review Tool is to be used for reviewing source code by one or more reviewers. This tool should be used parallel to static code checkers and other heuristics to allow focusing on aspects on reviewing code that require specifically human intelligence, for example, for justifying the correnctness of complex business logic and filtering malicious changes.

# Asynchronous code review

QGears review tool has some unique features:

 * Review is based on files - not commits.
 * The review process is not synchronized with development. Commits are possible without being reviewed but they will invalidate the review status of the file.
 * Review tool is designed to work with different version control systems; SVN and GIT are currently supported.
 * The data model of the reviews is human readable line oriented text file that can also be stored in version control. The review files are processed by the review tool to generate review status reports for a specific version of the source code (eg. a tag or branch of the code).
 * Delta reviews are supported by invoking third party diff tool (meld) to compare the current version to the latest reviewed version.

# Main usage scenario

First, one or more source file sets are to be defined on a specific branch of a source code management system (further abbreviated as 'SCM'), the files of which are the subjects to reviews. Reviewers then review the source files of these file sets.

More specifically, reviewers review a specific SCM revision of a file, and associate a status and a textual description to it. By the [status assigned by the reviewers](hu.qgears.review/src/hu/qgears/review/model/EReviewAnnotation.java), a [file-level status will be assigned](hu.qgears.review/src/hu/qgears/review/report/ReviewStatus.java) and will be presented in a report for all reviewed files:
* OK: the file is all right and accepted for release
* TODO: at least one developer has reviewed the current version of the source, and found some defects, that must be fixed
* OFF: the file should not be the part of the file set
* WON'T REVIEW: according at least one reviewer, the (current version of a) source file is not his or her responsibility to review and must be delegated to another team

If an already reviewed file changes, i. e. a new revision of it is committed to the SCM, the reviews for that file become obsolete for the new revision, marking the file with the status called 'OLD'. In this case, the affected file has to be reviewed again by all the reviewers to reach a complete state for the new revision. Note that changing a file does not invalidate a completed review process for a previous revision.

A reviewer may mark other, previously written reviews for a specific file as invalid, be the invalidated review or reviews created by him- or herself or other reviewers.

A review process is considered as completed for a revision if, and only if there is at least two non-obsolete, non-invalidated review with 'OK' status for every files for that revision, from at least two reviewers.

# Feature summary

* reviewing files
* defining source file sets, the files of which are to be reviewed
* support of the process of maintaining and modifying the source sets
* delegating responsibility of reviewing a file, to help communication of distinct teams having to review a large set of files together 
* review report generation
* review process traceability

# User interface

A browser-based and an Eclipse user interface helps to manage the review process, the latter with a special perspective, helping the evaluation of the current status of a review process by creating statistics both
* specific to the whole process and 
* specific to a single reviewer.
