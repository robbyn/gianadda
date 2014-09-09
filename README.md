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

<dl>
<dt>-c|--create &lt;theme-name&gt;</dt>

    Create a new gallery using the given theme, in the given directory.
    When used, -s and -f are implied.

<dt>-t|--change-theme &lt;theme-name&gt;</dt>
<dd>
    Change the theme of the gallery in the given directory.
    When used, -s and -f are implied.
</dd>
<dt>-s|--sync</dt>
<dd>
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

</dd>
<dt>-f|--force-html</dt>
<dd>
    Force the generation of the html (and css, js, ...) files of the web
    site, whether there was a change or not. When used, -s is implied.
</dd>
<dt>-v|--verbose</dt>
<dd>
    Display information messages. Without this flag, only warnings and
    errors are displayed.
</dd>
<dt>-q|--quiet</dt>
<dd>
    Don't display warning messages (only error messages)
</dd>
<dt>--debug</dt>
<dd>
    Display ALL messages, including debug messages. There can be quiet a lot.
</dd>
</dl>
