export default {
  spec_dir: "spec",
  spec_files: [
    "**/*.test.?(m)js"
  ],
  helpers: [
    "helpers/**/*.?(m)js",
    "helpers/reporter.js"
  ],
  env: {
    stopSpecOnExpectationFailure: false,
    random: true,
    forbidDuplicateNames: true
  }
}
