Perscope
==============================
These are the modified AspectJ sourcres for perscope implementation.


About Perscope
==============================

An aspect encapsulates not only crosscutting behavior, but also crosscutting state. When aspects are stateful, there is 
a need to specify and control their instantiation. Unfortunately, aspect instantiation is a hard-wired feature in 
ASPECTJ. This feature cannot be customized by the application programmer. Specifically, there are six predefined 
instantiation strategies to chose from, each designated by a keyword: issingleton, perthis, pertarget, percflow, 
percflowbelow, pertypewithin. In this work, we introduce a new language keyword perscope and a mechanism that lets 
third-parties define custom aspect instantiation strategies. This new keyword replaces the six existing keywords in 
ASPECTJ, and reduces the need for introducing future ones. The perscope mechanism developed by Victor Trakhtenberg in 
cooperation with David H. Lorenz at the Open University of Israel.

The accepted version for Software Composition 2011 can be found here: http://aop.cslab.openu.ac.il/research/perscope/mainSC11.pdf
