{ pkgs ? import <nixpkgs> {} }: with pkgs;
pkgs.mkShell {
  nativeBuildInputs = [ clojure boot nodejs_latest jq ];
}
