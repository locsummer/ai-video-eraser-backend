import SwiftUI

enum DrawingTool: String, CaseIterable {
    case track = "Track AI"
    case rectangle = "Rectangle"
    case ellipse = "Ellipse"
    case brush = "Brush"
}

struct MaskCanvasView: View {
    let activeTool: DrawingTool
    let brushRadius: CGFloat
    let onMaskUpdated: (RectDto?, [PointDto]?, [PointDto]?) -> Void

    @State private var dragStart: CGPoint?
    @State private var dragEnd: CGPoint?
    @State private var brushPoints: [CGPoint] = []
    @State private var trackingPoints: [CGPoint] = []

    var body: some View {
        Canvas { context, size in
            let maskColor = Color(red: 236/255, green: 72/255, blue: 153/255, opacity: 0.5)
            let strokeColor = Color(red: 244/255, green: 63/255, blue: 94/255)

            // Draw Brush Stroke
            if brushPoints.count > 1 {
                var path = Path()
                path.move(to: brushPoints[0])
                for pt in brushPoints.dropFirst() {
                    path.addLine(to: pt)
                }
                context.stroke(
                    path,
                    with: .color(maskColor),
                    style: StrokeStyle(lineWidth: brushRadius * 2, lineCap: .round, lineJoin: .round)
                )
            }

            // Draw Rectangle
            if activeTool == .rectangle, let start = dragStart, let end = dragEnd {
                let rect = CGRect(
                    x: min(start.x, end.x),
                    y: min(start.y, end.y),
                    width: abs(end.x - start.x),
                    height: abs(end.y - start.y)
                )
                context.fill(Path(rect), with: .color(maskColor))
                context.stroke(Path(rect), with: .color(strokeColor), lineWidth: 3)
            }

            // Draw Ellipse
            if activeTool == .ellipse, let start = dragStart, let end = dragEnd {
                let rect = CGRect(
                    x: min(start.x, end.x),
                    y: min(start.y, end.y),
                    width: abs(end.x - start.x),
                    height: abs(end.y - start.y)
                )
                context.fill(Path(ellipseIn: rect), with: .color(maskColor))
                context.stroke(Path(ellipseIn: rect), with: .color(strokeColor), lineWidth: 3)
            }

            // Draw Tracking Points Target
            for pt in trackingPoints {
                let targetRect = CGRect(x: pt.x - 20, y: pt.y - 20, width: 40, height: 40)
                context.stroke(Path(ellipseIn: targetRect), with: .color(.cyan), lineWidth: 3)
                let centerDot = CGRect(x: pt.x - 4, y: pt.y - 4, width: 8, height: 8)
                context.fill(Path(ellipseIn: centerDot), with: .color(.white))
            }
        }
        .gesture(
            DragGesture(minimumDistance: 0)
                .onChanged { value in
                    if activeTool == .track {
                        trackingPoints = [value.location]
                        onMaskUpdated(nil, [PointDto(x: Double(value.location.x), y: Double(value.location.y), timeSeconds: nil, frameIndex: nil)], nil)
                    } else if activeTool == .brush {
                        brushPoints.append(value.location)
                    } else {
                        if dragStart == nil {
                            dragStart = value.startLocation
                        }
                        dragEnd = value.location
                    }
                }
                .onEnded { value in
                    if activeTool == .rectangle || activeTool == .ellipse {
                        if let start = dragStart, let end = dragEnd {
                            let rect = RectDto(
                                x: Double(min(start.x, end.x)),
                                y: Double(min(start.y, end.y)),
                                width: Double(abs(end.x - start.x)),
                                height: Double(abs(end.y - start.y)),
                                startFrame: nil,
                                endFrame: nil
                            )
                            onMaskUpdated(rect, nil, nil)
                        }
                    } else if activeTool == .brush {
                        let points = brushPoints.map { PointDto(x: Double($0.x), y: Double($0.y), timeSeconds: nil, frameIndex: nil) }
                        onMaskUpdated(nil, nil, points)
                    }
                }
        )
    }
}
