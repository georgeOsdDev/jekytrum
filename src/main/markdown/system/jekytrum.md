# [Jekytrum](https://github.com/georgeOsdDev/jekytrum)

### [Jekyll](http://jekyllrb.com) + [Xitrum](http://xitrum-framework.github.io/) = Jekytrum

Jekytrum is a simple static site generator inspired by [Jekyll](http://jekyllrb.com) and developed
on top of [Xitrum](http://xitrum-framework.github.io/).

## Philosophy

* **Reactive**
  Jekytrum works reactive with source content.
  When markdown files are created/modified/deleted, Jekytrum converts markdown to html automatically.
  And created html will given an url without server restart.
  (Live reloading feature will be available soon)

* **Hackable**
  You can use any custome converter as you like.
  There are 4 default converter based on [Scalamd](https://github.com/chirino/scalamd), [Pegdown](https://github.com/sirthias/pegdown), [GithubAPI](https://developer.github.com/v3/markdown/), and NonConverter.
  And you can use any custom theme as you like.

* **Portable**

  Jekytrum only needs JVM as middleware. It does not need any Database.
  All dependency are embedded with build.sbt.

## Usage

* Save (.markdown |.md) files into `src/main/markdown`.
* Start server with `sbt/sbt run` as Xitrum way.
* Routing will automatically assigned depend on markdown file path and name.
* If client requested url to directory, jekytrum try fallback to show `index.md`.

  Ex.
  - `src/main/markdown/sample.md` will be rendered on `http://localhost:8000/sample`
  - `src/main/markdown/parent/child.md` will be rendered on `http://localhost:8000/parent/child`
  - `http://localhost:8000/parent/` will be respond with `src/main/markdown/parent/index.md`

## Feature

 * Dynamic conversion
 * Rebootless routing
 * Asynchronous built-in Http(s) server based on Xitrum on Netty
 * Keyword-Search with power of ElasticSearch

## Todos

  * Default theme
  * Use hazelcast as datastore
  * Make scalable(Share converted content between instances via hazelcast)
  * Make runnable with `xitrum-package`
  * Add live reload feature
  * Add command line tool like `docpad`
  * Follow the [good parts from existing  systems](http://www.slant.co/topics/330/compare/~jekyll_vs_octopress_vs_docpad)
  * And fix [known Issues](https://github.com/georgeOsdDev/jekytrum/issues)

## Licence

Source code can be found on [github](https://github.com/georgeOsdDev/jekytrum), licenced under [MIT](http://opensource.org/licenses/mit-license.php).

Developed by [Takeharu.Oshida](http://about.me/takeharu.oshida).