%%% metadata
% define class
\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{bsuir}[2024/02/14 BSUIR ENSTD 2017 compatible class]
% configure paper and margins
\LoadClass[14pt,final,onecolumn,oneside,titlepage,openany]{extreport}
\RequirePackage[    
    a4paper,
    left=30mm,
    bottom=27mm,
    right=15mm,
    top=20mm,
    footskip=17mm
]{geometry}
\linespread{1.1}
\setlength{\parindent}{1.25cm}

%%% class options
\RequirePackage{kvoptions}
% init
\SetupKeyvalOptions{
    family=KVO,
    prefix=KVO@
}
% variant is one of: custom, labwork, courcework, diploma, practice
\DeclareStringOption[custom]{variant}
% load them
\ProcessKeyvalOptions{KVO}
% validate variant
\RequirePackage{xifthen}
\ifthenelse{
    \equal{\KVO@variant}{custom}\OR
    \equal{\KVO@variant}{labwork}\OR
    \equal{\KVO@variant}{courcework}\OR
    \equal{\KVO@variant}{diploma}\OR
    \equal{\KVO@variant}{practice}
}{}{
    \ClassError{bsuir}{Unknown variant. Use one of: custom, labwork, courcework,
        diploma, practice}{\KVO@variant variant not available}
}

%%% language
% translations
\RequirePackage[main=russian,english]{babel}
% fonts
\babelfont{rm}{Times New Roman}
\babelfont{sf}{Times New Roman}
\babelfont{tt}{Courier New}

%%% TeX improvements
% for ...DocumentCommand
\RequirePackage{xparse}

%%% misc
% break long URLs
\RequirePackage{xurl}
% to merge other pdf files into document
\RequirePackage{pdfpages}
% fancy quotes
\RequirePackage{csquotes}
% enable links
\RequirePackage[
    hidelinks,
    unicode,
    bookmarksdepth=3,
    breaklinks
]{hyperref}
% make metadata toc
\RequirePackage{bookmark}
% for rows of images
\RequirePackage{subcaption}
% bibliography style
\bibliographystyle{ugost2003}

%%% page numbering
\RequirePackage{fancyhdr}
% define style
\fancypagestyle{plain}{
    \fancyhf{}
    \rfoot{\thepage}
    \renewcommand{\headrulewidth}{0pt}
    \renewcommand{\footrulewidth}{0pt}
}
\pagestyle{plain}

%%% load bsuir packages
% headings, toc, appendices
\RequirePackage{bsuir-sectioning}

%%% lists
\RequirePackage{enumitem}
% itemize
\setlist[itemize]{
    label=--,
    leftmargin=1.4\parindent,
    topsep=0pt,
    itemsep=0pt,
    parsep=0pt
}
% enumerate
\setlist[enumerate]{
    leftmargin=1.5\parindent,
    topsep=0pt,
    itemsep=0pt,
    parsep=0pt
}

%%% figures
% figures factory
\RequirePackage{float}
\RequirePackage[
    aboveskip=1.2\baselineskip,
    belowskip=-0.3\baselineskip
]{caption}
\captionsetup[figure]{
    name=Рисунок,
    labelsep=endash
}
% \makefigure
\NewDocumentCommand{\makefigure}{
    O{img} % figure prefix
    m % figure name
    m % code to make figure
}{
    \vspace{0.5\baselineskip}
    \begin{figure}[H]
        \centering
        #3
        \label{#1:#2}
    \end{figure}
    \vspace{-0.6\baselineskip}
}

%%% images
% \makeimage
\RequirePackage{graphicx}
\NewDocumentCommand{\makeimage}{
    o % caption
    m % filename
    O{width=\textwidth} % image width
}{
    \makefigure{#2}{
        \includegraphics[#3]{#2}
        \IfValueT{#1}{\caption{#1}}
    }
}
% \makesvg
\RequirePackage{svg}
\NewDocumentCommand{\makesvg}{
    o % caption
    m % filename
    O{width=\textwidth} % image width
}{
    \makefigure{#2}{
        \includesvg[inkscapelatex=false,#3]{#2}
        \IfValueT{#1}{\caption{#1}}
    }
}

%%% listings
\RequirePackage{minted}
\setminted{
    fontsize=\fontsize{12}{\baselineskip},
    tabsize=2,
    breaklines,
    mathescape,
    stripnl,
    breakanywhere,
    breaksymbolleft=,
    breaksymbolright=,
    breakbeforesymbolpre=,
    breakaftersymbolpre=,
    breakanywheresymbolpre=,
    breakautoindent=false,
}
% unite counters
\BeforeBeginEnvironment{document}{
    \makeatletter
    \let\c@figure\c@listing
    \makeatother
}
% set caption
\renewcommand\listingscaption{Рисунок}
\newenvironment{listingcaption}
{\captionsetup{type=listing,name=Рисунок,labelsep=endash}}{}
% fix listings numbering
\renewcommand{\thelisting}{\arabic{chapter}.\arabic{listing}}
% \makelisting
\NewDocumentCommand{\makelisting}{
    m % file
    o % caption
}{
    \begin{listingcaption}
        \centering
        \inputminted{text}{#1}
        \IfValueT{#2}{\caption{#2}~\\}
        \label{lst:#1}
    \end{listingcaption}
}

%%% tables
\RequirePackage{longtable}
% caption
\captionsetup[longtable]{
    name=Таблица,
    labelsep=endash,
    justification=raggedright,
    singlelinecheck=false
}
% margins
\LTpre=\baselineskip
\LTpost=\baselineskip

%%% translations
\addto\captionsrussian{
    \renewcommand{\contentsname}{Содержание}
    \renewcommand{\bibname}{Список использованных источников}
}

%%% template variables
\newcommand{\faculty}[1]{\def\@faculty{#1}}
\newcommand{\departmentlong}[1]{\def\@departmentlong{#1}}
\newcommand{\departmentshort}[1]{\def\@departmentshort{#1}}
\newcommand{\manager}[1]{\def\@manager{#1}}
\newcommand{\worktitle}[1]{\def\@worktitle{#1}}
\newcommand{\workcode}[1]{\def\@workcode{#1}}
\newcommand{\titlepageyear}[1]{\def\@titlepageyear{#1}}
\newcommand{\titleright}[1]{\def\@titleright{#1}}
\newcommand{\titlecenter}[1]{\def\@titlecenter{#1}}
\newcommand{\titleleft}[1]{\def\@titleleft{#1}}
\newcommand{\group}[1]{\def\@group{#1}}
\newcommand{\student}[1]{\def\@student{#1}}
\newcommand{\coursename}[1]{\def\@coursename{#1}}

%%% titlepage
\renewcommand{\maketitle}{
    \begin{titlepage}
        % all
        \centering
        Министерство образования Республики Беларусь\\
        [\the\baselineskip]
        Учреждение образования\\
        \MakeUppercase{
            Белорусский Государственный Университет\\
            Информатики и Радиоэлектроники
        }\\
        [\the\baselineskip]

        
        \ifthenelse{\equal{\KVO@variant}{diploma}}{
            % diploma
            \raggedright
            \begin{tabular}{l l}
                Факультет & \@faculty        \\
                Кафедра   & \@departmentlong
            \end{tabular}\\
            [\the\baselineskip]
        }{
        % other
        \raggedright
        \begin{tabular}{l l l}
            Факультет & \@faculty        \\
            Кафедра   & \@departmentlong \\
            Дисциплина & \@coursename
        \end{tabular}\\
        [\dimexpr\the\baselineskip * 3]
        }

        \ifthenelse{\equal{\KVO@variant}{diploma}}{
        % diploma
        \raggedleft
        \begin{tabular}{l}
            \textit{К защите допустить}:          \\\\
            Заведующий кафедрой \@departmentshort \\
            \hrulefill \@manager
        \end{tabular}\\
        [\the\baselineskip]
        }{
        % other
        ~\\[\dimexpr\the\baselineskip * 4]
        }

        \ifthenelse{\equal{\KVO@variant}{custom}}{
        % custom
        \@worktitle\\
        [\dimexpr\the\baselineskip * 7]
        }{\ifthenelse{\equal{\KVO@variant}{practice}}{
        % practice
        \MakeUppercase{Отчет по учебной (\@worktitle) практике}\\
        [\dimexpr\the\baselineskip * 7]
        }{\ifthenelse{\equal{\KVO@variant}{labwork}}{
        % labwork
        \center
        Лабораторная работа \textnumero\@workcode\\
        \textquote{\@worktitle}\\
        [\dimexpr\the\baselineskip * 7]
        }{
        % courcework, diploma
        \centering
        \MakeUppercase{Пояснительная записка}\\
        \ifthenelse{
            \equal{\KVO@variant}{courcework}
        }{к курсовой работе}{к дипломной работе}\\
        на тему\\
        [\the\baselineskip]
        \textbf{\MakeUppercase{\@worktitle}}\\
        [\the\baselineskip]
        \@workcode\\
        [\the\baselineskip]
        }}}

        \ifthenelse{\equal{\KVO@variant}{practice}}{
            % practice
            \begin{tabular}{l}
                Студент группы \@group \\\\
                Руководитель практики  \\
                от кафедры \@departmentshort
            \end{tabular}\hfill
            \begin{tabular}{c c l}
                \hrulefill & \hspace{30pt} & \@student \\
                \textsuperscript{(подпись)}            \\
                \hrulefill &               & \@manager \\
                ~
            \end{tabular}
        }{\ifthenelse{\equal{\KVO@variant}{courcework}}{
            % courcework
            \vspace{4\baselineskip}
            \begin{tabular}[t]{l}
                Студент группы \@group \\\\
                Руководитель  \\
            \end{tabular}\hfill
            \begin{tabular}[t]{c c l}
                \hrulefill & \hspace{30pt} & \@student \\
                \textsuperscript{(подпись)}            \\
                \hrulefill &               & \@manager \\
                \textsuperscript{(подпись)}            \\
                ~
            \end{tabular}
        
            % other
            \begin{tabular}{l}
                \@titleleft
            \end{tabular}\hfill
            \begin{tabular}{l}
                \@titleright
            \end{tabular}
        }}{\ifthenelse{\equal{\KVO@variant}{labwork}}{
            % courcework
            \vspace{4\baselineskip}
            \begin{tabular}[t]{l}
                Студент группы \@group \\\\
                Руководитель  \\
            \end{tabular}\hfill
            \begin{tabular}[t]{c c l}
                \hrulefill & \hspace{30pt} & \@student \\
                \textsuperscript{(подпись)}            \\
                \hrulefill &               & \@manager \\
                \textsuperscript{(подпись)}            \\
                ~
            \end{tabular}
        
            % other
            \begin{tabular}{l}
                \@titleleft
            \end{tabular}\hfill
            \begin{tabular}{l}
                \@titleright
            \end{tabular}
        }}

        % all
        \vfill
        \centering
        Минск \@titlepageyear
    \end{titlepage}
    \stepcounter{page}
}
