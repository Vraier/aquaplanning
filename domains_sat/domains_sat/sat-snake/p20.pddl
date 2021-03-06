(define (problem snake-empty-9x9-1-5-73-22499)
(:domain snake)
(:objects
    pos0-0 pos0-1 pos0-2 pos0-3 pos0-4 pos0-5 pos0-6 pos0-7 pos0-8 pos1-0 pos1-1 pos1-2 pos1-3 pos1-4 pos1-5 pos1-6 pos1-7 pos1-8 pos2-0 pos2-1 pos2-2 pos2-3 pos2-4 pos2-5 pos2-6 pos2-7 pos2-8 pos3-0 pos3-1 pos3-2 pos3-3 pos3-4 pos3-5 pos3-6 pos3-7 pos3-8 pos4-0 pos4-1 pos4-2 pos4-3 pos4-4 pos4-5 pos4-6 pos4-7 pos4-8 pos5-0 pos5-1 pos5-2 pos5-3 pos5-4 pos5-5 pos5-6 pos5-7 pos5-8 pos6-0 pos6-1 pos6-2 pos6-3 pos6-4 pos6-5 pos6-6 pos6-7 pos6-8 pos7-0 pos7-1 pos7-2 pos7-3 pos7-4 pos7-5 pos7-6 pos7-7 pos7-8 pos8-0 pos8-1 pos8-2 pos8-3 pos8-4 pos8-5 pos8-6 pos8-7 pos8-8
)
(:init
    (ISADJACENT pos0-0 pos1-0)
    (ISADJACENT pos0-0 pos0-1)
    (ISADJACENT pos0-1 pos1-1)
    (ISADJACENT pos0-1 pos0-2)
    (ISADJACENT pos0-1 pos0-0)
    (ISADJACENT pos0-2 pos1-2)
    (ISADJACENT pos0-2 pos0-3)
    (ISADJACENT pos0-2 pos0-1)
    (ISADJACENT pos0-3 pos1-3)
    (ISADJACENT pos0-3 pos0-4)
    (ISADJACENT pos0-3 pos0-2)
    (ISADJACENT pos0-4 pos1-4)
    (ISADJACENT pos0-4 pos0-5)
    (ISADJACENT pos0-4 pos0-3)
    (ISADJACENT pos0-5 pos1-5)
    (ISADJACENT pos0-5 pos0-6)
    (ISADJACENT pos0-5 pos0-4)
    (ISADJACENT pos0-6 pos1-6)
    (ISADJACENT pos0-6 pos0-7)
    (ISADJACENT pos0-6 pos0-5)
    (ISADJACENT pos0-7 pos1-7)
    (ISADJACENT pos0-7 pos0-8)
    (ISADJACENT pos0-7 pos0-6)
    (ISADJACENT pos0-8 pos1-8)
    (ISADJACENT pos0-8 pos0-7)
    (ISADJACENT pos1-0 pos2-0)
    (ISADJACENT pos1-0 pos1-1)
    (ISADJACENT pos1-0 pos0-0)
    (ISADJACENT pos1-1 pos2-1)
    (ISADJACENT pos1-1 pos1-2)
    (ISADJACENT pos1-1 pos0-1)
    (ISADJACENT pos1-1 pos1-0)
    (ISADJACENT pos1-2 pos2-2)
    (ISADJACENT pos1-2 pos1-3)
    (ISADJACENT pos1-2 pos0-2)
    (ISADJACENT pos1-2 pos1-1)
    (ISADJACENT pos1-3 pos2-3)
    (ISADJACENT pos1-3 pos1-4)
    (ISADJACENT pos1-3 pos0-3)
    (ISADJACENT pos1-3 pos1-2)
    (ISADJACENT pos1-4 pos2-4)
    (ISADJACENT pos1-4 pos1-5)
    (ISADJACENT pos1-4 pos0-4)
    (ISADJACENT pos1-4 pos1-3)
    (ISADJACENT pos1-5 pos2-5)
    (ISADJACENT pos1-5 pos1-6)
    (ISADJACENT pos1-5 pos0-5)
    (ISADJACENT pos1-5 pos1-4)
    (ISADJACENT pos1-6 pos2-6)
    (ISADJACENT pos1-6 pos1-7)
    (ISADJACENT pos1-6 pos0-6)
    (ISADJACENT pos1-6 pos1-5)
    (ISADJACENT pos1-7 pos2-7)
    (ISADJACENT pos1-7 pos1-8)
    (ISADJACENT pos1-7 pos0-7)
    (ISADJACENT pos1-7 pos1-6)
    (ISADJACENT pos1-8 pos2-8)
    (ISADJACENT pos1-8 pos0-8)
    (ISADJACENT pos1-8 pos1-7)
    (ISADJACENT pos2-0 pos3-0)
    (ISADJACENT pos2-0 pos2-1)
    (ISADJACENT pos2-0 pos1-0)
    (ISADJACENT pos2-1 pos3-1)
    (ISADJACENT pos2-1 pos2-2)
    (ISADJACENT pos2-1 pos1-1)
    (ISADJACENT pos2-1 pos2-0)
    (ISADJACENT pos2-2 pos3-2)
    (ISADJACENT pos2-2 pos2-3)
    (ISADJACENT pos2-2 pos1-2)
    (ISADJACENT pos2-2 pos2-1)
    (ISADJACENT pos2-3 pos3-3)
    (ISADJACENT pos2-3 pos2-4)
    (ISADJACENT pos2-3 pos1-3)
    (ISADJACENT pos2-3 pos2-2)
    (ISADJACENT pos2-4 pos3-4)
    (ISADJACENT pos2-4 pos2-5)
    (ISADJACENT pos2-4 pos1-4)
    (ISADJACENT pos2-4 pos2-3)
    (ISADJACENT pos2-5 pos3-5)
    (ISADJACENT pos2-5 pos2-6)
    (ISADJACENT pos2-5 pos1-5)
    (ISADJACENT pos2-5 pos2-4)
    (ISADJACENT pos2-6 pos3-6)
    (ISADJACENT pos2-6 pos2-7)
    (ISADJACENT pos2-6 pos1-6)
    (ISADJACENT pos2-6 pos2-5)
    (ISADJACENT pos2-7 pos3-7)
    (ISADJACENT pos2-7 pos2-8)
    (ISADJACENT pos2-7 pos1-7)
    (ISADJACENT pos2-7 pos2-6)
    (ISADJACENT pos2-8 pos3-8)
    (ISADJACENT pos2-8 pos1-8)
    (ISADJACENT pos2-8 pos2-7)
    (ISADJACENT pos3-0 pos4-0)
    (ISADJACENT pos3-0 pos3-1)
    (ISADJACENT pos3-0 pos2-0)
    (ISADJACENT pos3-1 pos4-1)
    (ISADJACENT pos3-1 pos3-2)
    (ISADJACENT pos3-1 pos2-1)
    (ISADJACENT pos3-1 pos3-0)
    (ISADJACENT pos3-2 pos4-2)
    (ISADJACENT pos3-2 pos3-3)
    (ISADJACENT pos3-2 pos2-2)
    (ISADJACENT pos3-2 pos3-1)
    (ISADJACENT pos3-3 pos4-3)
    (ISADJACENT pos3-3 pos3-4)
    (ISADJACENT pos3-3 pos2-3)
    (ISADJACENT pos3-3 pos3-2)
    (ISADJACENT pos3-4 pos4-4)
    (ISADJACENT pos3-4 pos3-5)
    (ISADJACENT pos3-4 pos2-4)
    (ISADJACENT pos3-4 pos3-3)
    (ISADJACENT pos3-5 pos4-5)
    (ISADJACENT pos3-5 pos3-6)
    (ISADJACENT pos3-5 pos2-5)
    (ISADJACENT pos3-5 pos3-4)
    (ISADJACENT pos3-6 pos4-6)
    (ISADJACENT pos3-6 pos3-7)
    (ISADJACENT pos3-6 pos2-6)
    (ISADJACENT pos3-6 pos3-5)
    (ISADJACENT pos3-7 pos4-7)
    (ISADJACENT pos3-7 pos3-8)
    (ISADJACENT pos3-7 pos2-7)
    (ISADJACENT pos3-7 pos3-6)
    (ISADJACENT pos3-8 pos4-8)
    (ISADJACENT pos3-8 pos2-8)
    (ISADJACENT pos3-8 pos3-7)
    (ISADJACENT pos4-0 pos5-0)
    (ISADJACENT pos4-0 pos4-1)
    (ISADJACENT pos4-0 pos3-0)
    (ISADJACENT pos4-1 pos5-1)
    (ISADJACENT pos4-1 pos4-2)
    (ISADJACENT pos4-1 pos3-1)
    (ISADJACENT pos4-1 pos4-0)
    (ISADJACENT pos4-2 pos5-2)
    (ISADJACENT pos4-2 pos4-3)
    (ISADJACENT pos4-2 pos3-2)
    (ISADJACENT pos4-2 pos4-1)
    (ISADJACENT pos4-3 pos5-3)
    (ISADJACENT pos4-3 pos4-4)
    (ISADJACENT pos4-3 pos3-3)
    (ISADJACENT pos4-3 pos4-2)
    (ISADJACENT pos4-4 pos5-4)
    (ISADJACENT pos4-4 pos4-5)
    (ISADJACENT pos4-4 pos3-4)
    (ISADJACENT pos4-4 pos4-3)
    (ISADJACENT pos4-5 pos5-5)
    (ISADJACENT pos4-5 pos4-6)
    (ISADJACENT pos4-5 pos3-5)
    (ISADJACENT pos4-5 pos4-4)
    (ISADJACENT pos4-6 pos5-6)
    (ISADJACENT pos4-6 pos4-7)
    (ISADJACENT pos4-6 pos3-6)
    (ISADJACENT pos4-6 pos4-5)
    (ISADJACENT pos4-7 pos5-7)
    (ISADJACENT pos4-7 pos4-8)
    (ISADJACENT pos4-7 pos3-7)
    (ISADJACENT pos4-7 pos4-6)
    (ISADJACENT pos4-8 pos5-8)
    (ISADJACENT pos4-8 pos3-8)
    (ISADJACENT pos4-8 pos4-7)
    (ISADJACENT pos5-0 pos6-0)
    (ISADJACENT pos5-0 pos5-1)
    (ISADJACENT pos5-0 pos4-0)
    (ISADJACENT pos5-1 pos6-1)
    (ISADJACENT pos5-1 pos5-2)
    (ISADJACENT pos5-1 pos4-1)
    (ISADJACENT pos5-1 pos5-0)
    (ISADJACENT pos5-2 pos6-2)
    (ISADJACENT pos5-2 pos5-3)
    (ISADJACENT pos5-2 pos4-2)
    (ISADJACENT pos5-2 pos5-1)
    (ISADJACENT pos5-3 pos6-3)
    (ISADJACENT pos5-3 pos5-4)
    (ISADJACENT pos5-3 pos4-3)
    (ISADJACENT pos5-3 pos5-2)
    (ISADJACENT pos5-4 pos6-4)
    (ISADJACENT pos5-4 pos5-5)
    (ISADJACENT pos5-4 pos4-4)
    (ISADJACENT pos5-4 pos5-3)
    (ISADJACENT pos5-5 pos6-5)
    (ISADJACENT pos5-5 pos5-6)
    (ISADJACENT pos5-5 pos4-5)
    (ISADJACENT pos5-5 pos5-4)
    (ISADJACENT pos5-6 pos6-6)
    (ISADJACENT pos5-6 pos5-7)
    (ISADJACENT pos5-6 pos4-6)
    (ISADJACENT pos5-6 pos5-5)
    (ISADJACENT pos5-7 pos6-7)
    (ISADJACENT pos5-7 pos5-8)
    (ISADJACENT pos5-7 pos4-7)
    (ISADJACENT pos5-7 pos5-6)
    (ISADJACENT pos5-8 pos6-8)
    (ISADJACENT pos5-8 pos4-8)
    (ISADJACENT pos5-8 pos5-7)
    (ISADJACENT pos6-0 pos7-0)
    (ISADJACENT pos6-0 pos6-1)
    (ISADJACENT pos6-0 pos5-0)
    (ISADJACENT pos6-1 pos7-1)
    (ISADJACENT pos6-1 pos6-2)
    (ISADJACENT pos6-1 pos5-1)
    (ISADJACENT pos6-1 pos6-0)
    (ISADJACENT pos6-2 pos7-2)
    (ISADJACENT pos6-2 pos6-3)
    (ISADJACENT pos6-2 pos5-2)
    (ISADJACENT pos6-2 pos6-1)
    (ISADJACENT pos6-3 pos7-3)
    (ISADJACENT pos6-3 pos6-4)
    (ISADJACENT pos6-3 pos5-3)
    (ISADJACENT pos6-3 pos6-2)
    (ISADJACENT pos6-4 pos7-4)
    (ISADJACENT pos6-4 pos6-5)
    (ISADJACENT pos6-4 pos5-4)
    (ISADJACENT pos6-4 pos6-3)
    (ISADJACENT pos6-5 pos7-5)
    (ISADJACENT pos6-5 pos6-6)
    (ISADJACENT pos6-5 pos5-5)
    (ISADJACENT pos6-5 pos6-4)
    (ISADJACENT pos6-6 pos7-6)
    (ISADJACENT pos6-6 pos6-7)
    (ISADJACENT pos6-6 pos5-6)
    (ISADJACENT pos6-6 pos6-5)
    (ISADJACENT pos6-7 pos7-7)
    (ISADJACENT pos6-7 pos6-8)
    (ISADJACENT pos6-7 pos5-7)
    (ISADJACENT pos6-7 pos6-6)
    (ISADJACENT pos6-8 pos7-8)
    (ISADJACENT pos6-8 pos5-8)
    (ISADJACENT pos6-8 pos6-7)
    (ISADJACENT pos7-0 pos8-0)
    (ISADJACENT pos7-0 pos7-1)
    (ISADJACENT pos7-0 pos6-0)
    (ISADJACENT pos7-1 pos8-1)
    (ISADJACENT pos7-1 pos7-2)
    (ISADJACENT pos7-1 pos6-1)
    (ISADJACENT pos7-1 pos7-0)
    (ISADJACENT pos7-2 pos8-2)
    (ISADJACENT pos7-2 pos7-3)
    (ISADJACENT pos7-2 pos6-2)
    (ISADJACENT pos7-2 pos7-1)
    (ISADJACENT pos7-3 pos8-3)
    (ISADJACENT pos7-3 pos7-4)
    (ISADJACENT pos7-3 pos6-3)
    (ISADJACENT pos7-3 pos7-2)
    (ISADJACENT pos7-4 pos8-4)
    (ISADJACENT pos7-4 pos7-5)
    (ISADJACENT pos7-4 pos6-4)
    (ISADJACENT pos7-4 pos7-3)
    (ISADJACENT pos7-5 pos8-5)
    (ISADJACENT pos7-5 pos7-6)
    (ISADJACENT pos7-5 pos6-5)
    (ISADJACENT pos7-5 pos7-4)
    (ISADJACENT pos7-6 pos8-6)
    (ISADJACENT pos7-6 pos7-7)
    (ISADJACENT pos7-6 pos6-6)
    (ISADJACENT pos7-6 pos7-5)
    (ISADJACENT pos7-7 pos8-7)
    (ISADJACENT pos7-7 pos7-8)
    (ISADJACENT pos7-7 pos6-7)
    (ISADJACENT pos7-7 pos7-6)
    (ISADJACENT pos7-8 pos8-8)
    (ISADJACENT pos7-8 pos6-8)
    (ISADJACENT pos7-8 pos7-7)
    (ISADJACENT pos8-0 pos8-1)
    (ISADJACENT pos8-0 pos7-0)
    (ISADJACENT pos8-1 pos8-2)
    (ISADJACENT pos8-1 pos7-1)
    (ISADJACENT pos8-1 pos8-0)
    (ISADJACENT pos8-2 pos8-3)
    (ISADJACENT pos8-2 pos7-2)
    (ISADJACENT pos8-2 pos8-1)
    (ISADJACENT pos8-3 pos8-4)
    (ISADJACENT pos8-3 pos7-3)
    (ISADJACENT pos8-3 pos8-2)
    (ISADJACENT pos8-4 pos8-5)
    (ISADJACENT pos8-4 pos7-4)
    (ISADJACENT pos8-4 pos8-3)
    (ISADJACENT pos8-5 pos8-6)
    (ISADJACENT pos8-5 pos7-5)
    (ISADJACENT pos8-5 pos8-4)
    (ISADJACENT pos8-6 pos8-7)
    (ISADJACENT pos8-6 pos7-6)
    (ISADJACENT pos8-6 pos8-5)
    (ISADJACENT pos8-7 pos8-8)
    (ISADJACENT pos8-7 pos7-7)
    (ISADJACENT pos8-7 pos8-6)
    (ISADJACENT pos8-8 pos7-8)
    (ISADJACENT pos8-8 pos8-7)
    (tailsnake pos1-8)
    (headsnake pos0-8)
    (nextsnake pos0-8 pos1-8)
    (blocked pos0-8)
    (blocked pos1-8)
    (spawn pos3-7)
    (NEXTSPAWN pos4-3 dummypoint)
    (NEXTSPAWN pos3-7 pos7-6)
    (NEXTSPAWN pos7-6 pos7-5)
    (NEXTSPAWN pos7-5 pos0-5)
    (NEXTSPAWN pos0-5 pos6-2)
    (NEXTSPAWN pos6-2 pos3-0)
    (NEXTSPAWN pos3-0 pos0-7)
    (NEXTSPAWN pos0-7 pos8-6)
    (NEXTSPAWN pos8-6 pos8-7)
    (NEXTSPAWN pos8-7 pos7-7)
    (NEXTSPAWN pos7-7 pos7-2)
    (NEXTSPAWN pos7-2 pos2-6)
    (NEXTSPAWN pos2-6 pos0-6)
    (NEXTSPAWN pos0-6 pos8-0)
    (NEXTSPAWN pos8-0 pos5-6)
    (NEXTSPAWN pos5-6 pos7-1)
    (NEXTSPAWN pos7-1 pos0-0)
    (NEXTSPAWN pos0-0 pos4-1)
    (NEXTSPAWN pos4-1 pos4-5)
    (NEXTSPAWN pos4-5 pos1-4)
    (NEXTSPAWN pos1-4 pos0-4)
    (NEXTSPAWN pos0-4 pos5-7)
    (NEXTSPAWN pos5-7 pos6-0)
    (NEXTSPAWN pos6-0 pos3-5)
    (NEXTSPAWN pos3-5 pos3-2)
    (NEXTSPAWN pos3-2 pos2-8)
    (NEXTSPAWN pos2-8 pos8-5)
    (NEXTSPAWN pos8-5 pos2-2)
    (NEXTSPAWN pos2-2 pos3-3)
    (NEXTSPAWN pos3-3 pos5-5)
    (NEXTSPAWN pos5-5 pos7-8)
    (NEXTSPAWN pos7-8 pos4-2)
    (NEXTSPAWN pos4-2 pos6-3)
    (NEXTSPAWN pos6-3 pos5-0)
    (NEXTSPAWN pos5-0 pos6-5)
    (NEXTSPAWN pos6-5 pos5-2)
    (NEXTSPAWN pos5-2 pos2-1)
    (NEXTSPAWN pos2-1 pos2-4)
    (NEXTSPAWN pos2-4 pos4-6)
    (NEXTSPAWN pos4-6 pos6-7)
    (NEXTSPAWN pos6-7 pos1-3)
    (NEXTSPAWN pos1-3 pos0-2)
    (NEXTSPAWN pos0-2 pos2-5)
    (NEXTSPAWN pos2-5 pos3-4)
    (NEXTSPAWN pos3-4 pos2-0)
    (NEXTSPAWN pos2-0 pos7-4)
    (NEXTSPAWN pos7-4 pos6-6)
    (NEXTSPAWN pos6-6 pos5-4)
    (NEXTSPAWN pos5-4 pos8-8)
    (NEXTSPAWN pos8-8 pos6-1)
    (NEXTSPAWN pos6-1 pos4-7)
    (NEXTSPAWN pos4-7 pos1-6)
    (NEXTSPAWN pos1-6 pos3-1)
    (NEXTSPAWN pos3-1 pos5-3)
    (NEXTSPAWN pos5-3 pos4-8)
    (NEXTSPAWN pos4-8 pos7-0)
    (NEXTSPAWN pos7-0 pos1-7)
    (NEXTSPAWN pos1-7 pos3-6)
    (NEXTSPAWN pos3-6 pos2-7)
    (NEXTSPAWN pos2-7 pos4-0)
    (NEXTSPAWN pos4-0 pos2-3)
    (NEXTSPAWN pos2-3 pos8-3)
    (NEXTSPAWN pos8-3 pos6-4)
    (NEXTSPAWN pos6-4 pos4-4)
    (NEXTSPAWN pos4-4 pos8-1)
    (NEXTSPAWN pos8-1 pos0-3)
    (NEXTSPAWN pos0-3 pos5-1)
    (NEXTSPAWN pos5-1 pos1-1)
    (NEXTSPAWN pos1-1 pos3-8)
    (NEXTSPAWN pos3-8 pos1-0)
    (NEXTSPAWN pos1-0 pos6-8)
    (NEXTSPAWN pos6-8 pos8-2)
    (NEXTSPAWN pos8-2 pos4-3)
    (ispoint pos1-2)
    (ispoint pos5-8)
    (ispoint pos1-5)
    (ispoint pos7-3)
    (ispoint pos0-1)
)
(:goal
(and
    (not (ispoint pos1-2))
    (not (ispoint pos5-8))
    (not (ispoint pos1-5))
    (not (ispoint pos7-3))
    (not (ispoint pos0-1))
    (not (ispoint pos3-7))
    (not (ispoint pos7-6))
    (not (ispoint pos7-5))
    (not (ispoint pos0-5))
    (not (ispoint pos6-2))
    (not (ispoint pos3-0))
    (not (ispoint pos0-7))
    (not (ispoint pos8-6))
    (not (ispoint pos8-7))
    (not (ispoint pos7-7))
    (not (ispoint pos7-2))
    (not (ispoint pos2-6))
    (not (ispoint pos0-6))
    (not (ispoint pos8-0))
    (not (ispoint pos5-6))
    (not (ispoint pos7-1))
    (not (ispoint pos0-0))
    (not (ispoint pos4-1))
    (not (ispoint pos4-5))
    (not (ispoint pos1-4))
    (not (ispoint pos0-4))
    (not (ispoint pos5-7))
    (not (ispoint pos6-0))
    (not (ispoint pos3-5))
    (not (ispoint pos3-2))
    (not (ispoint pos2-8))
    (not (ispoint pos8-5))
    (not (ispoint pos2-2))
    (not (ispoint pos3-3))
    (not (ispoint pos5-5))
    (not (ispoint pos7-8))
    (not (ispoint pos4-2))
    (not (ispoint pos6-3))
    (not (ispoint pos5-0))
    (not (ispoint pos6-5))
    (not (ispoint pos5-2))
    (not (ispoint pos2-1))
    (not (ispoint pos2-4))
    (not (ispoint pos4-6))
    (not (ispoint pos6-7))
    (not (ispoint pos1-3))
    (not (ispoint pos0-2))
    (not (ispoint pos2-5))
    (not (ispoint pos3-4))
    (not (ispoint pos2-0))
    (not (ispoint pos7-4))
    (not (ispoint pos6-6))
    (not (ispoint pos5-4))
    (not (ispoint pos8-8))
    (not (ispoint pos6-1))
    (not (ispoint pos4-7))
    (not (ispoint pos1-6))
    (not (ispoint pos3-1))
    (not (ispoint pos5-3))
    (not (ispoint pos4-8))
    (not (ispoint pos7-0))
    (not (ispoint pos1-7))
    (not (ispoint pos3-6))
    (not (ispoint pos2-7))
    (not (ispoint pos4-0))
    (not (ispoint pos2-3))
    (not (ispoint pos8-3))
    (not (ispoint pos6-4))
    (not (ispoint pos4-4))
    (not (ispoint pos8-1))
    (not (ispoint pos0-3))
    (not (ispoint pos5-1))
    (not (ispoint pos1-1))
    (not (ispoint pos3-8))
    (not (ispoint pos1-0))
    (not (ispoint pos6-8))
    (not (ispoint pos8-2))
    (not (ispoint pos4-3))
)
)
)