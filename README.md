# Jekytrum = Jekyll + Xitrum

Simple static site generator.

### Usage:

  * Save (.markdown|.md) files into `src/main/markdown`.
  * Start server with `sbt/sbt run` as xitrum way.
  * Routing is depend on markdown file name.

    Ex. `sample.md` will be rendered on `http://localhost:8000/sample`


### TODOs:

  * Implement basic features.
   * read and compile markdown in async with Akka
   * livereload with file monitor
   * Add created_at/updated_at
   * list entries at index page
   * tags
   * :white_check_mark: routing with directory tree.
   * theme/style
   * i18n
  * Tune xitrum
   * remove unused pipeline handler
   * load content at handler
   * use LruCache instead of MMap
   * hack xitrum-package
  * Follow the good parts from existing systems
    (http://www.slant.co/topics/330/compare/~jekyll_vs_octopress_vs_docpad)
