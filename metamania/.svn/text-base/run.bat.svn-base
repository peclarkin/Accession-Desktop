@echo off
if [%1] == [] echo no parameters, assume just run
if NOT [%2] == [] pushd plugins\%2
if NOT [%1] == [] call ant %1
if NOT [%2] == [] popd
if NOT [%2] == [] call ant make-brands
pushd dist\accession
call ant run
popd
