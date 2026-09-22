package com.example.projektmobilki.mechanics.chess

import kotlin.math.abs

enum class PieceColor { WHITE, BLACK }
enum class PieceType { PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING }

data class Piece(val type: PieceType, val color: PieceColor)
data class Position(val r: Int, val c: Int)
data class Move(
    val from: Position,
    val to: Position,
    val promotion: PieceType? = null,
    val isEnPassant: Boolean = false,
    val isCastling: Boolean = false
)

class ChessEngine(initialize: Boolean = true) {
    var board: Array<Array<Piece?>> = Array(8) { arrayOfNulls(8) }
    var turn: PieceColor = PieceColor.WHITE

    var whiteCaptured = mutableListOf<PieceType>()
    var blackCaptured = mutableListOf<PieceType>()

    var isCheck = false
    var isCheckmate = false
    var isStalemate = false

    var whiteKingMoved = false
    var whiteRookKingsideMoved = false
    var whiteRookQueensideMoved = false

    var blackKingMoved = false
    var blackRookKingsideMoved = false
    var blackRookQueensideMoved = false

    var enPassantTarget: Position? = null

    init {
        if (initialize) {
            setupBoard()
            updateGameStatus()
        }
    }

    private fun setupBoard() {
        val backRank = arrayOf(PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN, PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK)
        for (i in 0..7) {
            board[0][i] = Piece(backRank[i], PieceColor.BLACK)
            board[1][i] = Piece(PieceType.PAWN, PieceColor.BLACK)
            board[6][i] = Piece(PieceType.PAWN, PieceColor.WHITE)
            board[7][i] = Piece(backRank[i], PieceColor.WHITE)
        }
    }

    fun clone(): ChessEngine {
        val engine = ChessEngine(initialize = false)
        for (r in 0..7) {
            for (c in 0..7) {
                engine.board[r][c] = this.board[r][c]
            }
        }
        engine.turn = this.turn
        engine.whiteKingMoved = this.whiteKingMoved
        engine.whiteRookKingsideMoved = this.whiteRookKingsideMoved
        engine.whiteRookQueensideMoved = this.whiteRookQueensideMoved
        engine.blackKingMoved = this.blackKingMoved
        engine.blackRookKingsideMoved = this.blackRookKingsideMoved
        engine.blackRookQueensideMoved = this.blackRookQueensideMoved
        engine.enPassantTarget = this.enPassantTarget?.copy()
        return engine
    }

    fun getLegalMoves(pos: Position): List<Move> {
        val piece = board[pos.r][pos.c] ?: return emptyList()
        if (piece.color != turn) return emptyList()

        val pseudoLegal = getPseudoLegalMoves(pos, board)
        val legal = mutableListOf<Move>()

        for (move in pseudoLegal) {
            val cloned = this.clone()
            cloned.applyMove(move, isSimulation = true)
            if (!cloned.isKingInCheck(turn)) {
                // Check castling passing through check
                if (move.isCastling) {
                    val step = if (move.to.c > move.from.c) 1 else -1
                    val passPos = Position(pos.r, pos.c + step)
                    val cloned2 = this.clone()
                    cloned2.applyMove(Move(pos, passPos), isSimulation = true)
                    if (!cloned2.isKingInCheck(turn) && !this.isKingInCheck(turn)) {
                        legal.add(move)
                    }
                } else {
                    legal.add(move)
                }
            }
        }
        return legal
    }

    private fun getPseudoLegalMoves(pos: Position, b: Array<Array<Piece?>>): List<Move> {
        val piece = b[pos.r][pos.c] ?: return emptyList()
        val moves = mutableListOf<Move>()
        val dir = if (piece.color == PieceColor.WHITE) -1 else 1

        val addPawnMove = { to: Position, isEnP: Boolean ->
            if (to.r == 0 || to.r == 7) {
                moves.add(Move(pos, to, promotion = PieceType.QUEEN, isEnPassant = isEnP))
                moves.add(Move(pos, to, promotion = PieceType.ROOK, isEnPassant = isEnP))
                moves.add(Move(pos, to, promotion = PieceType.BISHOP, isEnPassant = isEnP))
                moves.add(Move(pos, to, promotion = PieceType.KNIGHT, isEnPassant = isEnP))
            } else {
                moves.add(Move(pos, to, isEnPassant = isEnP))
            }
        }

        when (piece.type) {
            PieceType.PAWN -> {
                if (isValid(pos.r + dir, pos.c) && b[pos.r + dir][pos.c] == null) {
                    addPawnMove(Position(pos.r + dir, pos.c), false)
                    val startRow = if (piece.color == PieceColor.WHITE) 6 else 1
                    if (pos.r == startRow && b[pos.r + dir * 2][pos.c] == null) {
                        moves.add(Move(pos, Position(pos.r + dir * 2, pos.c)))
                    }
                }
                for (dc in listOf(-1, 1)) {
                    if (isValid(pos.r + dir, pos.c + dc)) {
                        val target = b[pos.r + dir][pos.c + dc]
                        if (target != null && target.color != piece.color) {
                            addPawnMove(Position(pos.r + dir, pos.c + dc), false)
                        } else if (enPassantTarget == Position(pos.r + dir, pos.c + dc)) {
                            addPawnMove(Position(pos.r + dir, pos.c + dc), true)
                        }
                    }
                }
            }
            PieceType.KNIGHT -> {
                val jumps = listOf(-2 to -1, -2 to 1, -1 to -2, -1 to 2, 1 to -2, 1 to 2, 2 to -1, 2 to 1)
                for ((dr, dc) in jumps) {
                    val nr = pos.r + dr; val nc = pos.c + dc
                    if (isValid(nr, nc) && b[nr][nc]?.color != piece.color) {
                        moves.add(Move(pos, Position(nr, nc)))
                    }
                }
            }
            PieceType.BISHOP -> {
                moves.addAll(getSlidingMoves(pos, b, listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)))
            }
            PieceType.ROOK -> {
                moves.addAll(getSlidingMoves(pos, b, listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)))
            }
            PieceType.QUEEN -> {
                moves.addAll(getSlidingMoves(pos, b, listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1, -1 to 0, 1 to 0, 0 to -1, 0 to 1)))
            }
            PieceType.KING -> {
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        if (dr == 0 && dc == 0) continue
                        val nr = pos.r + dr; val nc = pos.c + dc
                        if (isValid(nr, nc) && b[nr][nc]?.color != piece.color) {
                            moves.add(Move(pos, Position(nr, nc)))
                        }
                    }
                }
                val kingRow = if (piece.color == PieceColor.WHITE) 7 else 0
                val kingMoved = if (piece.color == PieceColor.WHITE) whiteKingMoved else blackKingMoved
                val rKMoved = if (piece.color == PieceColor.WHITE) whiteRookKingsideMoved else blackRookKingsideMoved
                val rQMoved = if (piece.color == PieceColor.WHITE) whiteRookQueensideMoved else blackRookQueensideMoved

                if (!kingMoved && pos.r == kingRow && pos.c == 4) {
                    if (!rKMoved && b[kingRow][5] == null && b[kingRow][6] == null && b[kingRow][7]?.type == PieceType.ROOK) {
                        moves.add(Move(pos, Position(kingRow, 6), isCastling = true))
                    }
                    if (!rQMoved && b[kingRow][3] == null && b[kingRow][2] == null && b[kingRow][1] == null && b[kingRow][0]?.type == PieceType.ROOK) {
                        moves.add(Move(pos, Position(kingRow, 2), isCastling = true))
                    }
                }
            }
        }
        return moves
    }

    private fun getSlidingMoves(pos: Position, b: Array<Array<Piece?>>, dirs: List<Pair<Int, Int>>): List<Move> {
        val moves = mutableListOf<Move>()
        val color = b[pos.r][pos.c]?.color ?: return emptyList()
        for ((dr, dc) in dirs) {
            var nr = pos.r + dr
            var nc = pos.c + dc
            while (isValid(nr, nc)) {
                val target = b[nr][nc]
                if (target == null) {
                    moves.add(Move(pos, Position(nr, nc)))
                } else {
                    if (target.color != color) {
                        moves.add(Move(pos, Position(nr, nc)))
                    }
                    break
                }
                nr += dr
                nc += dc
            }
        }
        return moves
    }

    private fun isValid(r: Int, c: Int) = r in 0..7 && c in 0..7

    private fun applyMove(move: Move, isSimulation: Boolean) {
        val piece = board[move.from.r][move.from.c] ?: return
        val target = board[move.to.r][move.to.c]

        if (target != null && !isSimulation) {
            if (turn == PieceColor.WHITE) whiteCaptured.add(target.type) else blackCaptured.add(target.type)
        }

        var enPassantNext: Position? = null
        if (piece.type == PieceType.PAWN) {
            if (abs(move.from.r - move.to.r) == 2) {
                enPassantNext = Position((move.from.r + move.to.r) / 2, move.from.c)
            }
            if (move.isEnPassant) {
                val captureRow = if (turn == PieceColor.WHITE) move.to.r + 1 else move.to.r - 1
                val capturedPawn = board[captureRow][move.to.c]
                if (!isSimulation && capturedPawn != null) {
                    if (turn == PieceColor.WHITE) whiteCaptured.add(capturedPawn.type) else blackCaptured.add(capturedPawn.type)
                }
                board[captureRow][move.to.c] = null
            }
        }
        enPassantTarget = enPassantNext

        if (move.isCastling) {
            val r = move.to.r
            if (move.to.c == 6) {
                board[r][5] = board[r][7]
                board[r][7] = null
            } else if (move.to.c == 2) {
                board[r][3] = board[r][0]
                board[r][0] = null
            }
        }

        board[move.to.r][move.to.c] = piece
        board[move.from.r][move.from.c] = null

        if (move.promotion != null) {
            board[move.to.r][move.to.c] = Piece(move.promotion, piece.color)
        }

        if (piece.type == PieceType.KING) {
            if (piece.color == PieceColor.WHITE) whiteKingMoved = true else blackKingMoved = true
        }
        if (piece.type == PieceType.ROOK) {
            if (move.from.r == 7 && move.from.c == 7) whiteRookKingsideMoved = true
            if (move.from.r == 7 && move.from.c == 0) whiteRookQueensideMoved = true
            if (move.from.r == 0 && move.from.c == 7) blackRookKingsideMoved = true
            if (move.from.r == 0 && move.from.c == 0) blackRookQueensideMoved = true
        }
    }

    fun makeMove(move: Move) {
        applyMove(move, false)
        turn = if (turn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        updateGameStatus()
    }

    private fun isKingInCheck(color: PieceColor): Boolean {
        var kingPos: Position? = null
        for (r in 0..7) {
            for (c in 0..7) {
                val p = board[r][c]
                if (p?.type == PieceType.KING && p.color == color) {
                    kingPos = Position(r, c)
                }
            }
        }
        if (kingPos == null) return false

        for (r in 0..7) {
            for (c in 0..7) {
                val p = board[r][c]
                if (p != null && p.color != color) {
                    val moves = getPseudoLegalMoves(Position(r, c), board)
                    if (moves.any { it.to == kingPos }) return true
                }
            }
        }
        return false
    }

    private fun updateGameStatus() {
        isCheck = isKingInCheck(turn)
        isCheckmate = false
        isStalemate = false
        
        var hasLegalMoves = false
        outer@ for (r in 0..7) {
            for (c in 0..7) {
                val p = board[r][c]
                if (p != null && p.color == turn) {
                    if (getLegalMoves(Position(r, c)).isNotEmpty()) {
                        hasLegalMoves = true
                        break@outer
                    }
                }
            }
        }

        if (!hasLegalMoves) {
            if (isCheck) isCheckmate = true
            else isStalemate = true
        }
    }
}
