# Jekytrum = Jekyll + Xitrum

Simple static site generator.

### Usage:

  * Save (.markdown|.md) files into `src/main/markdown`.
  * Start server with `sbt/sbt run` as xitrum way.
  * Routing is depend on markdown file name.

    Ex. `sample.md` will be rendered on `http://localhost:8000/sample`


### TODOs:

  * Implement basic features.
   * :white_check_mark: routing with directory tree.
   * :white_check_mark: compile markdown in async with Future
   * :white_check_mark: Add lastModified
   * :white_check_mark: livereload with file monitor
   * :white_check_mark: categories
   * :white_check_mark: list entries at index page
   * :white_check_mark: next,prev utility
   * theme/style
   * i18n
   * Embed elastic search
  * Tune xitrum
   * :white_check_mark: load content at handler
   * ~~remove unused pipeline handler~~
   * ~~use LruCache instead of MMap~~
   * hack xitrum-package to work with `xitrum-package`ed module
  * Follow the good parts from existing systems
    (http://www.slant.co/topics/330/compare/~jekyll_vs_octopress_vs_docpad)
