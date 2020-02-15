![](https://steamcdn-a.akamaihd.net/steam/apps/392020/header.jpg?t=1581736470)

# RFLEX
[RFLEX is now $0](https://store.steampowered.com/app/392020/RFLEX/). Here's the source code.

## Warning
This code is bad. REALLY bad. This was my low point as a programmer. I never intended this for public consumption. The majority of this code was thrown together in one sleep-deprived summer. I was the only programmer, so don't expect good commenting (if any). I'm only releasing this because [someone asked for it](https://steamcommunity.com/app/392020/discussions/0/1746770817528847981/?tscn=1581755926). If you want to look through this, it's your funeral.

## Hints
I'm definitely not going to maintain this codebase, but if anyone wants to set up a development environment I have a few hints for you:
* RFLEX is made in **Java/libGDX**, and requires **gradle** to run. I've stripped out most development environment files (you would've found them to be of little use) except for some gradle configurations in the top-level. These files will tell you what packages are required to set it up.
* There are still some Steam SDK calls in there, so remove them until it runs.
* I made a decent level editor, but it's hidden somewhere in the files. If you are planning on extending the gameplay, I would highly recommend looking into that.

## P.S:
I am releasing these files in good faith; please use your common sense and don't abuse that.

## Licensing
As per the GPLv3.0 license, you can use this code for whatever, just don't sell anything based off of it.
