# Jekytrum = Jekyll + Xitrum

Simple static site generator.


### Usage:

  * Save (.markdown |.md) files into `src/main/markdown`.
  * Start server with `sbt/sbt run` as xitrum way.
  * Routing is depend on markdown file path and name.
  * If client requested url to directory, jekytrum try fallback index.md

    Ex. `src/main/markdown/sample.md` will be rendered on `http://localhost:8000/sample`
        `src/main/markdown/parent/child.md` will be rendered on `http://localhost:8000/parent/child`
        `src/main/markdown/parent/child.md` will be rendered on `http://localhost:8000/parent/child`
        `http://localhost:8000/parent/` will be respond with `src/main/markdown/parent/index.md`

  * Once jekytrum server started, file event (create/modify/delete) on markdown files are monitored,
    And jekytrum work reactive to these event and respond with newest converted result.
  * You can select your favorite markdown converter from (Scalamd, Pegdown or GithubAPI)

### TODOs:

  * Implement basic features.
   * :white_check_mark: routing with directory tree.
   * :white_check_mark: compile markdown in async with Future
   * :white_check_mark: Add lastModified
   * :white_check_mark: livereload with file monitor
   * :white_check_mark: categories
   * :white_check_mark: list entries at index page
   * :white_check_mark: next,prev utility
   * :white_check_mark: theme
   * default style
   * ~~i18n~~
   * Embed elastic search
  * Tune xitrum
   * :white_check_mark: load content at handler
   * ~~remove unused pipeline handler~~
   * ~~use LruCache instead of MMap~~
   * hack xitrum-package to work with `xitrum-package`ed module
  * Follow the good parts from existing systems
    (http://www.slant.co/topics/330/compare/~jekyll_vs_octopress_vs_docpad)
