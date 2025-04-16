# quarkus-jdbi

[![Build](https://github.com/quarkiverse/quarkus-jdbi/workflows/Build/badge.svg?branch=main)](https://github.com/quarkiverse/quarkus-jdbi/actions/workflows/build.yml)
[![License](https://img.shields.io/github/license/quarkiverse/quarkus-jdbi)](http://www.apache.org/licenses/LICENSE-2.0)
[![Central](https://img.shields.io/maven-central/v/io.quarkiverse.jdbi/quarkus-jdbi-parent?color=green)](https://search.maven.org/search?q=g:io.quarkiverse.jdbi%20AND%20a:quarkus-jdbi-parent)

<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-4-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->


Jdbi provides convenient, idiomatic access to relational data in Java
This quarkus extension makes it possible to use JDBI in native executables.

# Example usage

 Add the following dependency in your pom.xml to get started,

```xml
<dependency>
    <groupId>io.quarkiverse.jdbi</groupId>
    <artifactId>quarkus-jdbi</artifactId>
</dependency>
```

You need to inject AgroalDatasource:

```java
public class JdbiProvider {
    @Inject
    AgroalDataSource ds;

    @Singleton
    @Produces
    public Jdbi jdbi() {
        Jdbi jdbi = Jdbi.create(ds);
        jdbi.installPlugin(new SqlObjectPlugin());
        return jdbi;
    }
}
```

and you can use it everywhere:

```java
public class UserDAO {
    @Inject
    Jdbi jdbi;

    public Optional<User> findUserById(long id) {
        return jdbi.inTransaction(transactionHandle ->
                transactionHandle.createQuery("SELECT * FROM users WHERE id=:id")
                        .bind("id", id)
                        .mapTo(User.class)
                        .findFirst());
    }
}
```

# Authors

Original repo: https://github.com/famartinrh/jdbi-quarkus-extension

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/smil2k"><img src="https://avatars.githubusercontent.com/u/2590036?v=4?s=100" width="100px;" alt="Tamas"/><br /><sub><b>Tamas</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-jdbi/commits?author=smil2k" title="Code">💻</a> <a href="#maintenance-smil2k" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://amaechler.com"><img src="https://avatars.githubusercontent.com/u/1240238?v=4?s=100" width="100px;" alt="Andreas Maechler"/><br /><sub><b>Andreas Maechler</b></sub></a><br /><a href="#maintenance-amaechler" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="http://www.diversit.eu"><img src="https://avatars.githubusercontent.com/u/484565?v=4?s=100" width="100px;" alt="Joost den Boer"/><br /><sub><b>Joost den Boer</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-jdbi/commits?author=diversit" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="http://phinjensen.com"><img src="https://avatars.githubusercontent.com/u/1008789?v=4?s=100" width="100px;" alt="Phin Jensen"/><br /><sub><b>Phin Jensen</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-jdbi/commits?author=phinjensen" title="Code">💻</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!
