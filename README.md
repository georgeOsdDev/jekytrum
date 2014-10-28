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
   * livereload with file monitor
   * list entries at index page
   * theme/style
   * i18n
   * next,prev utility
   * tags
   * Embed elastic search
  * Tune xitrum
   * remove unused pipeline handler
   * load content at handler
   * use LruCache instead of MMap
   * hack xitrum-package
  * Follow the good parts from existing systems
    (http://www.slant.co/topics/330/compare/~jekyll_vs_octopress_vs_docpad)
