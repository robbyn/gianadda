Gianadda
========

Yet another gallery generator.

Usage:
------

```sh
gianadda [(-c|--create) <theme-name>] [(-t|--change-theme) <theme-name>]
    [-s|--sync] [-f|--force-html] <gallery-directory>
```

Options:

`-c`|`--create` <theme-name>
    Create a new gallery using the given theme, in the given directory.
    When used, -s and -f are implied.

`-t`|`--change-theme` <theme-name>
    Change the theme of the gallery in the given directory.
    When used, -s and -f are implied.

`-s`|`--sync`
    Synchronize the given gallery to reflect changes in the content of the
    gallery directory:
        .   create new folder when new directories are found
        .   create new pictures when new image files are found, and generate
            thumbnails and preview files.
        .   update picture info in the catalog when image files are changed,
            and regenerate thumbnails and preview files.
    NOTE:
        The folders and pictures that no longer exist in the directory
        structure are kept in the catalog.

`-f`|`--force-html`
    Force the generation of the html (and css, js, ...) files of the web
    site, whether there was a change or not. When used, -s is implied.

`-v`|`--verbose`
    Display information messages. Without this flag, only warnings and
    errors are displayed.

`-q`|`--quiet`
    Don't display warning messages (only error messages)

`--debug`
    Display ALL messages, including debug messages.
