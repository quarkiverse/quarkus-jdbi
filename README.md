# quarkus-jdbi

[![Build](https://github.com/quarkiverse/quarkus-jdbi/workflows/Build/badge.svg?branch=main)](https://github.com/quarkiverse/quarkus-jdbi/actions/workflows/build.yml)
[![License](https://img.shields.io/github/license/quarkiverse/quarkus-jdbi)](http://www.apache.org/licenses/LICENSE-2.0)
[![Central](https://img.shields.io/maven-central/v/io.quarkiverse.jdbi/quarkus-jdbi-parent?color=green)](https://search.maven.org/search?q=g:io.quarkiverse.jdbi%20AND%20a:quarkus-jdbi-parent)

<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-)
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
  <tr>
    <td align="center"><a href="https://github.com/smil2k"><img src="https://avatars.githubusercontent.com/u/2590036?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Tamas</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-jdbi/commits?author=smil2k" title="Code">💻</a> <a href="#maintenance-smil2k" title="Maintenance">🚧</a></td>
  </tr>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!
