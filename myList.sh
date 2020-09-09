#chmod +x Script1.sh is required for the script to run
#install txt2html in case one doesn't have it
sudo apt install txt2html
#ls in current directory, then write results to a html file called htmlFile.html
ls | txt2html > htmlFile.html
#sends htmlFile.html to firefox to be displayed
firefox ./htmlFile.html
