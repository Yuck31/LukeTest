package JettersR.Util;
/**
 * This is meant to be used as a means to reduce the number
 * of collision checks needed for Entity Collisions.
 * 
 * Author: Luke Sullivan
 * Last Edit: 3/8/2024
 */
import java.util.List;
import java.util.ArrayList;

//import org.joml.Vector3f;

import JettersR.Entities.Components.OctreeObject;
import JettersR.Util.Shapes.Shapes3D.Shape3D;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class Octree<T extends OctreeObject>
{
    protected int maxObjects = 16;
    protected int maxLevels = 16;

    protected int level;
    protected int x, y, z, width, height, depth;
    protected @fixed int f_xCenter, f_yCenter, f_zCenter;

    //Objects in this node.
    private List<T> objects;

    //Child nodes.
    private Octree<T>[] nodes;

    
    /**
     * Constructor.
     * 
     * @param level
     * @param x
     * @param y
     * @param z
     * @param width
     * @param height
     * @param depth
    */
    @SuppressWarnings("unchecked")//I need the warning suppress here because "Cannot create a generic array of Octree<T>"".
    public Octree(int level, int x, int y, int z, int width, int height, int depth)
    {
        this.level = level;
        //
        this.x = x;
        this.y = y;
        this.z = z;
        //
        this.width = width;
        this.height = height;
        this.depth = depth;
        //
        this.f_xCenter = fixed(x) + (fixed(width) / 2);
        this.f_yCenter = fixed(y) + (fixed(height) / 2);
        this.f_zCenter = fixed(z) + (fixed(depth) / 2);
        //
        objects = new ArrayList<T>();
        nodes = new Octree[8];
    }

    /**Clears this Octree.*/
    public void clear()
    {
        //Clear all nodes.
        for(int i = 0; i < nodes.length; i++)
        {
            if(nodes[i] != null)
            {
                nodes[i].clear();
                nodes[i] = null;
            }
        }

        //Clear this node's object list.
        objects.clear();
    }
    
    /**Creates nodes for this Octree.*/
    protected void split()
    {
        //Dividing a Cube into eighths in a 2d space is a bit hard to explain...
        int subWidth = (int)(width / 2),  subHeight = (int)(height / 2), subdepth = (int)(depth / 2);
        int x = this.x, y = this.y, z = this.z;
        /*
         * 45
         * 67
         *      Layered over each other to make cube, basically.
         * 01
         * 23
         */
        nodes[0] = new Octree<T>(level+1,
            x, y, z, subWidth, subHeight, subdepth);

        nodes[1] = new Octree<T>(level+1,
            x + subWidth, y, z , subWidth, subHeight, subdepth);

        nodes[2] = new Octree<T>(level+1,
            x, y + subHeight, z, subWidth, subHeight, subdepth);

        nodes[3] = new Octree<T>(level+1,
            x + subWidth, y + subHeight, z, subWidth, subHeight, subdepth);

        nodes[4] = new Octree<T>(level+1,
            x, y, z + subdepth, subWidth, subHeight, subdepth);

        nodes[5] = new Octree<T>(level+1,
            x + subWidth, y, z + subdepth , subWidth, subHeight, subdepth);

        nodes[6] = new Octree<T>(level+1,
            x, y + subHeight, z + subdepth, subWidth, subHeight, subdepth);

        nodes[7] = new Octree<T>(level+1,
            x + subWidth, y + subHeight, z + subdepth, subWidth, subHeight, subdepth);
    }


    /**getIndex(), but it takes raw coordinates and dimensions.*/
    protected int getIndex(@fixed int f_x, @fixed int f_y, @fixed int f_z, @fixed int f_width, @fixed int f_height, @fixed int f_depth)
    {
        byte rightSide =  0, downSide = 0, topSide = 0;

        //Which X-Section can the object fit in?
        if(f_x < this.f_xCenter && f_x + f_width < this.f_xCenter){rightSide = 0;}
        else if(f_x >= this.f_xCenter){rightSide = 0b001;}
        else{return -1;}

        //Which Y-Section can the object fit in?
        if(f_y < this.f_yCenter && f_y + f_height < this.f_yCenter){downSide = 0;}
        else if(f_y >= this.f_yCenter){downSide = 0b010;}
        else{return -1;}

        //Which Z-Section can the object fit in?
        if(f_z < this.f_zCenter && f_z + f_depth < this.f_zCenter){topSide = 0;}
        else if(f_z >= this.f_zCenter){topSide = 0b100;}
        else{return -1;}

        //Or the values together to get the index.
        return topSide | downSide | rightSide;
    }

    /**Retrives the node index the given Position and Shape resides in.*/
    protected int getIndex(fixedVector3 f_position, Shape3D shape)
    {
        //We just use the maximum dimensions of the shape, because I'm lazy.
        //return getIndex((int)position.x + shape.left(), (int)position.y + shape.back(), (int)position.z + shape.bottom(),
        return getIndex
        (
            f_position.x + shape.f_left(), f_position.y + shape.f_back(), f_position.z + shape.f_bottom(),
            shape.f_getWidth(), shape.f_getHeight(), shape.f_getDepth()
        );
    }

    /**Retrives the node index the given OctreeObject resides in.*/
    protected int getIndex(T o)
    {
        return getIndex(o.f_getPosition(), o.getShape());

        //This function uses programming quadrant logic, not real quadrant logic.
        //(Gotta keep things consistant!)

        /*
        byte rightSide =  0, downSide = 0, topSide = 0;

        //Which X-Section can the object fit in?
        if(o.left() < xCenter && o.right() < xCenter){rightSide = 0;}
        else if(o.left() >= xCenter){rightSide = 0b1;}
        else{return -1;}

        //Which Y-Section can the object fit in?
        if(o.back() < yCenter && o.front() < yCenter){downSide = 0;}
        else if(o.back() >= yCenter){downSide = 0b10;}
        else{return -1;}

        //Which Z-Section can the object fit in?
        if(o.bottom() < zCenter && o.top() < zCenter){topSide = 0;}
        else if(o.bottom() >= zCenter){topSide = 0b100;}
        else{return -1;}

        //Or the values together to get the index.
        return topSide | downSide | rightSide;
        */
    }


    /**Inserts a Collision Object into the Octree.*/
    public void insert(T object)
    {
        //Insert the Object into any nodes if nessesary.
        if(nodes[0] != null)
        {
            int index = getIndex(object);

            //If the object can fit in a node.
            if(index != -1)
            {
                //Put it in that octree node.
                nodes[index].insert(object);
                return;
            }
        }

        //If it couldn't fit into a child node, put it in this parent octree node instead.
        objects.add(object);

        //If this octree node is full and hasn't split to the maximum limit of levels.
        if(objects.size() > maxObjects && level < maxLevels)
        {
            //If there are no child nodes, create some (8 to be precise).
            if(nodes[0] == null){split();}

            //Put any smaller objects into those nodes.
            for(int i = 0; i < objects.size(); i++)
            {
                int index = getIndex(objects.get(i));
                if(index != -1){nodes[index].insert(objects.remove(i));}
            }
        }
    }

    /**Retrives the portion of this Octree associated with the given OctreeObject and puts it into listToFill.*/
    public void retrieve(T object, List<T> listToFill)
    {
        retrieve(object.f_getPosition(), object.getShape(), listToFill);
    }

    /**Retrives the portion of this Octree associated with the given position and shape and puts it into listToFill.*/
    public void retrieve(fixedVector3 f_position, Shape3D shape, List<T> listToFill)
    {
        //Create list to return.
        //List<T> returnObjects = new ArrayList<T>();

        //Which node does the shape fit in?
        int index = getIndex(f_position, shape);
        
        //If the shape completely fits in a node (if there are nodes to fit in...)
        if(index != -1 && nodes[0] != null)
        {
            //Perform retrive function on that node.
            //returnObjects =
            nodes[index].retrieve(f_position, shape, listToFill);
        }

        //Add all of this node's objects to the returning list and return it.
        listToFill.addAll(this.objects);
        //(Bigger nodes only contain bigger objects.)
        //return returnObjects;
    }
}
