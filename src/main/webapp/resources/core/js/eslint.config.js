import js from "@eslint/js";
import globals from "globals";
import jasmine from "eslint-plugin-jasmine";

export default [
  js.configs.recommended,
  {
    files: ["**/*.js"],
    languageOptions: {
      ecmaVersion: 2022,
      sourceType: "module",
      globals: {
        ...globals.browser,
        ...globals.jasmine,
      },
    },
    plugins: {
      jasmine,
    },
    rules: {
      "indent": ["error", 2],
      "linebreak-style": ["error", "unix"],
      "quotes": ["error", "double"],
      "semi": ["error", "always"],
      "no-unused-vars": "warn",
    },
  },
  {
    files: ["spec/**/*.js"],
    rules: {
      ...jasmine.configs.recommended.rules,
    },
  },
];
