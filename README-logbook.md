CVSROOT/ preserved in main for historical context.
Not part of active resurrection arc.
Git hygiene supersedes legacy CVS scaffolding.
- Removed legacy RCS ,v files from working tree
- Preserved originals in main branch for historical audit
- Asserted clean build hygiene for ant-based resurrection arc

- Verified Java 25 compatibility; deferred syntax audit for Java 8–17 fallback

## License Hygiene

This project uses the [Gradle License Report Plugin](https://github.com/jk1/Gradle-License-Report) to audit third-party dependencies.

Generated reports:
- `build/reports/licenses/licenses.html`: Human-readable summary
- `build/reports/licenses/licenses.json`: Machine-readable audit trail

All third-party libraries are used under their respective open-source licenses. See `licenses/` for details.

