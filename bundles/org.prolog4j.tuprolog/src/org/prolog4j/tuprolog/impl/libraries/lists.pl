/*  This code has been taken from SWI Prolog:
    https://www.swi-prolog.org/pldoc/doc/_SWI_/library/lists.pl
	It has been adjusted to fit the API of TuProlog (mainly deletion
	of things). The original license is given below.
*/

/*  Part of SWI-Prolog

    Author:        Jan Wielemaker and Richard O'Keefe
    E-mail:        J.Wielemaker@cs.vu.nl
    WWW:           http://www.swi-prolog.org
    Copyright (c)  2002-2016, University of Amsterdam
                              VU University Amsterdam
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in
       the documentation and/or other materials provided with the
       distribution.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
    "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
    LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
    FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
    COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
    BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
    CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
    LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
    ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
*/

memberchk(E, L) :-
	member(E, L).

%!  intersection(+Set1, +Set2, -Set3) is det.
%
%   True if Set3 unifies with the  intersection   of  Set1 and Set2. The
%   complexity of this predicate is |Set1|*|Set2|. A _set_ is defined to
%   be an unordered list  without   duplicates.  Elements are considered
%   duplicates if they can be unified.
%
%   @see ord_intersection/3.

intersection([], _, []) :- !.
intersection([X|T], L, Intersect) :-
    memberchk(X, L),
    !,
    Intersect = [X|R],
    intersection(T, L, R).
intersection([_|T], L, R) :-
    intersection(T, L, R).


%!  union(+Set1, +Set2, -Set3) is det.
%
%   True if Set3 unifies with the union of  the lists Set1 and Set2. The
%   complexity of this predicate is |Set1|*|Set2|. A _set_ is defined to
%   be an unordered list  without   duplicates.  Elements are considered
%   duplicates if they can be unified.
%
%   @see ord_union/3

union([], L, L) :- !.
union([H|T], L, R) :-
    memberchk(H, L),
    !,
    union(T, L, R).
union([H|T], L, [H|R]) :-
    union(T, L, R).


%!  subset(+SubSet, +Set) is semidet.
%
%   True if all elements of SubSet  belong   to  Set as well. Membership
%   test is based on memberchk/2. The   complexity  is |SubSet|*|Set|. A
%   _set_ is defined  to  be  an   unordered  list  without  duplicates.
%   Elements are considered duplicates if they can be unified.
%
%   @see ord_subset/2.

subset([], _) :- !.
subset([E|R], Set) :-
    memberchk(E, Set),
    subset(R, Set).


%!  subtract(+Set, +Delete, -Result) is det.
%
%   Delete all elements  in  Delete  from   Set.  Deletion  is  based on
%   unification using memberchk/2. The complexity   is |Delete|*|Set|. A
%   _set_ is defined  to  be  an   unordered  list  without  duplicates.
%   Elements are considered duplicates if they can be unified.
%
%   @see ord_subtract/3.

subtract([], _, []) :- !.
subtract([E|T], D, R) :-
    memberchk(E, D),
    !,
    subtract(T, D, R).
subtract([H|T], D, [H|R]) :-
    subtract(T, D, R).